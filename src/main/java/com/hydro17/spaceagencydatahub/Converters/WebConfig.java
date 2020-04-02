package com.hydro17.spaceagencydatahub.Converters;

import com.hydro17.spaceagencydatahub.services.MissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private MissionService missionService;

    public WebConfig(MissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new ProductDtoToProductConverter(missionService));
    }
}
