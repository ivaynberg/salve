package salve.depend.spring.txn.integ;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import org.hsqldb.Types;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

public class Database implements InitializingBean
{
    private DataSource dataSource;
    private PlatformTransactionManager transactionManager;
    private JdbcTemplate template;

    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }


    public void setTransactionManager(PlatformTransactionManager transactionManager)
    {
        this.transactionManager = transactionManager;
    }


    public void afterPropertiesSet() throws Exception
    {
        Connection con = dataSource.getConnection();
        Statement stmt = con.createStatement();
        stmt.executeUpdate("CREATE TABLE test (val VARCHAR(64));");
        stmt.close();
        con.close();

        template = new JdbcTemplate(dataSource);
    }

    public int count() throws Exception
    {
        return template.queryForInt("SELECT COUNT(*) FROM test");
    }

    @Transactional
    public void insert(String val) throws Exception
    {
        template.update("INSERT INTO test (val) VALUES(?)", new Object[] { val },
                new int[] { Types.VARCHAR });
    }

    @Transactional
    public void insertThatFails(String val) throws Exception
    {
        insert(val);
        throw new IllegalStateException();
    }

    @Transactional(noRollbackFor = IllegalStateException.class)
    public void insertThatSucceedsWithException(String val) throws Exception
    {
        insert(val);
        throw new IllegalStateException();
    }


    @SuppressWarnings({"unchecked"})
    public List<String> list() throws Exception
    {
        return template.queryForList("SELECT * FROM test ORDER BY val", String.class);
    }
}
