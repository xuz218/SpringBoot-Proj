package cn.com.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.RestController;

/**
* <p>Title: ServiceSeckillApplication</p>  
* <p>Description: seckill service服务启动类</p>  
* @author Ziang Xu
 */
@Configuration
@RestController
@SpringBootApplication
@EnableTransactionManagement //启用事务
public class ServiceSeckillApplication {


	public static void main( String[] args ){
    	SpringApplication.run(ServiceSeckillApplication.class, args);
    }
	
}
