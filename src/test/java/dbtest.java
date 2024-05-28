import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.sql.Connection;

import static junit.framework.TestCase.assertTrue;

@Import(dbtest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/main/webapp/WEB-INF/spring/**/root-context.xml"})
public class dbtest {
    @Autowired
    DataSource ds; // 컨테이너로부터 자동 주입받는다.

    @Autowired
    SqlSessionFactoryBean sf; // 컨테이너로부터 자동 주입받는다.

    @Test
    public void jdbcConnectionTest() throws Exception {

        System.out.println("ds = " + ds);

        Connection conn = ds.getConnection(); // 데이터베이스의 연결을 얻는다.

        System.out.println("conn = " + conn);
        assertTrue(conn!=null);
    }
}
