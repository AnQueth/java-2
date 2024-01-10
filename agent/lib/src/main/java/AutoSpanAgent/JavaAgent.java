package AutoSpanAgent;

import java.io.IOException;
import java.io.StringReader;
import java.lang.instrument.Instrumentation;
import java.util.Properties;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Identified.Extendable;
import net.bytebuddy.matcher.ElementMatchers;

public class JavaAgent {

    public static void premain(String agentArgs, Instrumentation instrumentation) throws InstantiationException {

        /*
         * InterceptingClassTransformer interceptingClassTransformer = new
         * InterceptingClassTransformer();
         * 
         * interceptingClassTransformer.init();
         * 
         * instrumentation.addTransformer(interceptingClassTransformer);
         */

        Properties props = new Properties();
        if (agentArgs != null) {
            try {
                props.load(new StringReader(agentArgs.replace(',', '\n')));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        String what = props.getProperty("what");
        boolean useMono = props.getProperty("usemono") != null;
        String ignores = props.getProperty("ignores");
        String[] ignoreList = null;
        if (ignores != null) {
            ignoreList = ignores.split("\\|");
        }

        AgentBuilder g = new AgentBuilder.Default()
                .type(ElementMatchers.nameStartsWithIgnoreCase(what).and(null != ignoreList
                        ? ElementMatchers.whereNone(ElementMatchers.namedOneOf(ignoreList))
                        : ElementMatchers.none()))
                .transform(new BuddyTransformer())
                .with(AgentBuilder.TypeStrategy.Default.REDEFINE);
        if (useMono) {
            g = g.type(ElementMatchers.named("reactor.core.publisher.Mono"))
                    .transform(new BuddyTransformer())
                    .with(AgentBuilder.TypeStrategy.Default.REDEFINE);
        }

        g.installOn(instrumentation);

    }

}