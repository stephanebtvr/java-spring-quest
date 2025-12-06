package com.javaquest.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing // Active les annotations @CreatedDate et @LastModifiedDate
public class JavaQuestBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(JavaQuestBackendApplication.class, args);

		 // Log de confirmation (visible dans la console)
        System.out.println("""
            
            ╔════════════════════════════════════════════════════════╗
                                                                 	 
                   🚀 JavaQuest Backend Started Successfully!   	     
                                                                    
                 📡 API: http://localhost:8080                        
                 📚 Swagger: http://localhost:8080/swagger-ui.html    
                 💾 Database: PostgreSQL                             
                                                                   
            ╚════════════════════════════════════════════════════════╝
            
            """);
	}
}
