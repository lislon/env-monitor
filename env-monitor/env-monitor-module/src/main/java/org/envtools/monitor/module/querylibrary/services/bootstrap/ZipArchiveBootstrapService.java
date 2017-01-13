package org.envtools.monitor.module.querylibrary.services.bootstrap;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.envtools.monitor.model.querylibrary.QueryParamType;
import org.envtools.monitor.model.querylibrary.db.Category;
import org.envtools.monitor.model.querylibrary.db.LibQuery;
import org.envtools.monitor.model.querylibrary.db.QueryParam;
import org.envtools.monitor.module.exception.BootstrapZipParseException;
import org.envtools.monitor.module.querylibrary.dao.CategoryDao;
import org.envtools.monitor.module.querylibrary.dao.LibQueryDao;
import org.envtools.monitor.module.querylibrary.dao.QueryParamDao;
import org.envtools.monitor.module.querylibrary.services.BootstrapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Created by IAvdeev on 11.01.2017.
 * Load the categories and queries from zip archive, which contains sql files in directories hierarchy.
 *
 * @example of file structure in zip
 * <code>
 *     Order
 *     +- List Orders
 *        + - List 10 latest orders.sql
 *        |   <file-contents>
 *        |     --DESCRIPTION: My description
 *        |
 *        |      SELECT * FROM orders ORDER BY date LIMIT 10
 *        |   </file-contents>
 *        + - Get order by id.sql
 *        |   <file-contents>
 *        |     --DESCRIPTION My description
 *        |     --PARAM id:NUMBER
 *        |
 *        |     SELECT * FROM orders WHERE id = :id
 *        |   </file-contents>
 * </code>
 */
@Transactional
public class ZipArchiveBootstrapService implements BootstrapService {
    private static final Logger LOGGER = Logger.getLogger(ZipArchiveBootstrapService.class);

    /**
     * Location of sql file relative to application classpath.
     */
    @Value("${querylibrary.zip_bootstrapper.location}")
    String fileLocation;

    @PersistenceContext
    protected EntityManager em;

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    QueryParamDao queryParamDao;

    @Autowired
    LibQueryDao libQueryDao;


    @Override
    public void bootstrap() throws Exception {

        descendIntoDirectory(Paths.get(fileLocation), null);
    }

    /**
     * Breadth-first searching starting from rootPath
     *
     * @param rootPath
     * @param rootCat   Parent directory, null for root
     * @throws IOException
     * @throws BootstrapZipParseException
     */
    private void descendIntoDirectory(Path rootPath, Category rootCat) throws IOException, BootstrapZipParseException
    {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(rootPath)) {
            for (Path path : directoryStream) {
                if (Files.isDirectory(path)) {
                    Category cat = createCategory(path, rootCat);
                    descendIntoDirectory(path, cat);
                } else {
                    createQuery(path, rootCat);
                }
            }
        }
    }

    private LibQuery createQuery(Path path, Category category) throws IOException, BootstrapZipParseException  {

        LOGGER.debug(String.format("Reading file '%s'", path.toString()));

        LibQuery libQuery = new LibQuery();
        libQuery.setCategory(category);
        // by default use filename as title for queries
        libQuery.setTitle(FilenameUtils.getBaseName(path.toString()));
        // save to get an id for saving libQueryParam
        libQueryDao.saveAndFlush(libQuery);

        boolean rawSqlPart = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(path.toString()));
            StringWriter rawSqlString = new StringWriter();
            PrintWriter rawSqlWr = new PrintWriter(rawSqlString)) {
            String line;

            Pattern pattern = Pattern.compile("^--(TITLE|DESCRIPTION|PARAM)\\s+(.+)$");

            for (int lineNumber = 1; (line = reader.readLine()) != null; lineNumber++) {
                Matcher matcher = pattern.matcher(line);
                // break at first non-keyword name
                if (!rawSqlPart && matcher.matches()) {

                    String name = matcher.group(1);
                    String value = matcher.group(2).trim();

                    switch (name) {
                        case "TITLE":
                            libQuery.setTitle(value);
                            break;
                        case "DESCRIPTION":
                            libQuery.setDescription(value);
                            break;
                        case "PARAM":
                            String[] nameAndType = value.split(":");
                            if (nameAndType.length != 2) {
                                throw new BootstrapZipParseException(String.format("Unexpected param format '%s' at line %d in '%s'", line, lineNumber, path.getFileName().toString()));
                            }

                            Collection<String> availableParamsTypes = Arrays.stream(QueryParamType.values())
                                    .map(Enum::name)
                                    .collect(Collectors.toList());

                            if (!availableParamsTypes.contains(nameAndType[1])) {
                                throw new BootstrapZipParseException(String.format("Unexpected param type '%s' at line %d in '%s'", line, lineNumber, path.getFileName().toString()));
                            }

                            QueryParam queryParam = new QueryParam(nameAndType[0], QueryParamType.valueOf(nameAndType[1]));
                            queryParam.setLibQuery(libQuery);
                            queryParamDao.save(queryParam);

//                            libQuery.getQueryParams().add(queryParam);
                            break;
                        default:
                            throw new BootstrapZipParseException(String.format("Unexpected param '%s' at line %d in '%s'", line, lineNumber, path.getFileName().toString()));
                    }
                } else {
                    // !matcher.matches()
                    rawSqlPart = true;
                    rawSqlWr.println(line);
                }
            }

            libQuery.setText(rawSqlString.toString().trim());
        }
        libQueryDao.save(libQuery);

        return libQuery;
    }

    private Category createCategory(Path path, Category parentCat) {

        Category cat = new Category();

        cat.setQueries(new ArrayList<>());
        cat.setTitle(path.getFileName().toString());
        cat.setParentCategory(parentCat);
        categoryDao.saveAndFlush(cat);

        return cat;
    }
}
