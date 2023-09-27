package cu.hash.camelsagatest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MapPropertySource;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootApplication
public class CamelSagaTestApplication {
    private static final Logger logger = LoggerFactory.getLogger(CamelSagaTestApplication.class);

    /**
     * Main method to start the application.
     */
    public static void main(String[] args) {

        new SpringApplicationBuilder(CamelSagaTestApplication.class)
                .initializers(new CustomApplicationInitializer())
                .run(args);
    }
    static class CustomApplicationInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        private static final Pattern interfaceAddressPlaceholderPattern = Pattern.compile("\\$\\{(interface\\.[^\\}]+)\\.host\\}");

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            ConfigurableEnvironment environment = applicationContext.getEnvironment();
            Map<String,Object> properties = new HashMap<>();

            environment.getPropertySources().forEach(source->{

                if (source instanceof EnumerablePropertySource){
                    EnumerablePropertySource<?> enumerablePropertySource =
                            (EnumerablePropertySource<?>) source;

                    Arrays.stream(enumerablePropertySource.getPropertyNames())
                            .forEach(name->{
                                Object value = enumerablePropertySource.getProperty(name);

                                if(value instanceof String){
                                    Matcher matcher = interfaceAddressPlaceholderPattern.matcher((String) value);
                                    while(matcher.find()){
                                        String propRoot = matcher.group(1);
                                        String iFaceName = environment.getProperty(propRoot);
                                        properties.put(propRoot+".host",resolveAddressFor(iFaceName));
                                    }

                                }
                            });
                }
                environment.getPropertySources().addFirst(new MapPropertySource("interface-properties",properties));
            });
        }

        private String resolveAddressFor(String ifaceName){
            String inetAddress = null;

            if (ifaceName != null){
                try {
                    inetAddress = NetworkUtils.getInet4AddressFor(ifaceName);

                    if(inetAddress==null){
                        logger.warn("Unable to resolve inet address for network interface with name {}",ifaceName);
                    }

                }catch (SocketException e ) {
                    logger.error("An error occurred while resolving the address for network interface with name {}", ifaceName, e);
                }
            }else{
                try{
                    inetAddress = InetAddress.getLocalHost().getHostAddress();
                }catch (UnknownHostException e ){
                    logger.error("An error has ocurred while resolving the localhost address", e);
                }
            }

            //fallback
            if(inetAddress==null){
                inetAddress= "locahost";
            }

            return inetAddress;

        }
    }

}
