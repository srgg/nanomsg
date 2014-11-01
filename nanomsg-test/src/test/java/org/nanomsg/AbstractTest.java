package org.nanomsg;

import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class AbstractTest {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected static String date(){
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(new Date());
    }

    @Rule
    public TestRule watchman = new TestWatcher() {
        @Override
        protected void starting(Description description) {
            super.starting(description);
            logger.info("\n\n" +
                            "===================================================================================================\n" +
                            "   TEST: {}\n" +
                            "===================================================================================================\n",
                    description.getDisplayName());
        }

        @Override
        protected void finished(Description description) {
            super.finished(description);
            logger.info("\n\n" +
                            "===================================================================================================\n" +
                            "   END {}\n" +
                            "===================================================================================================\n",
                    description.getDisplayName());
        }
    };
}
