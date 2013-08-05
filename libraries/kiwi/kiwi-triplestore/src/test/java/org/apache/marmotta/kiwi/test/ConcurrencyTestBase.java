package org.apache.marmotta.kiwi.test;

import com.google.code.tempusfugit.concurrency.ConcurrentRule;
import com.google.code.tempusfugit.concurrency.RepeatingRule;
import com.google.code.tempusfugit.concurrency.annotations.Concurrent;
import com.google.code.tempusfugit.concurrency.annotations.Repeating;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public abstract class ConcurrencyTestBase {

    protected static Repository repository;

    protected static Random rnd;

    private static long runs = 0;

    protected static Logger logger;

    @Rule
    public ConcurrentRule concurrently = new ConcurrentRule();

    @Rule
    public RepeatingRule repeatedly = new RepeatingRule();


    @Rule
    public TestWatcher watchman = new TestWatcher() {
        /**
         * Invoked when a test is about to start
         */
        @Override
        protected void starting(Description description) {
            logger.info("{} being run...", description.getMethodName());
        }
    };

    long tripleCount = 0;

    @Test
    @Concurrent(count = 10)
    @Repeating(repetition = 10)
    public void testConcurrency() throws Exception {
        runs++;

        // generate random nodes and triples and add them
        RepositoryConnection con = repository.getConnection();
        try {
            for(int i=0; i< rnd.nextInt(1000); i++) {
                URI subject = repository.getValueFactory().createURI("http://localhost/"+ RandomStringUtils.randomAlphanumeric(8));
                URI predicate = repository.getValueFactory().createURI("http://localhost/"+ RandomStringUtils.randomAlphanumeric(8));
                Value object;
                switch(rnd.nextInt(6)) {
                    case 0: object = repository.getValueFactory().createURI("http://localhost/"+ RandomStringUtils.randomAlphanumeric(8));
                        break;
                    case 1: object = repository.getValueFactory().createBNode();
                        break;
                    case 2: object = repository.getValueFactory().createLiteral(RandomStringUtils.randomAscii(40));
                        break;
                    case 3: object = repository.getValueFactory().createLiteral(rnd.nextInt());
                        break;
                    case 4: object = repository.getValueFactory().createLiteral(rnd.nextDouble());
                        break;
                    case 5: object = repository.getValueFactory().createLiteral(rnd.nextBoolean());
                        break;
                    default: object = repository.getValueFactory().createURI("http://localhost/"+ RandomStringUtils.randomAlphanumeric(8));
                        break;

                }
                con.add(subject,predicate,object);
                tripleCount++;
            }
            con.commit();
        } finally {
            con.close();
        }


        logger.info("triple count: {}", tripleCount);
    }
}