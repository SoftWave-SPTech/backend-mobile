package softwave.backend.backend_mobile.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String path = System.getProperty("user.dir") + "/uploads/perfis/";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + path);
    }
}