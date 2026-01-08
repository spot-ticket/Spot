package com.example.Spot.global;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

public abstract class TestSupport {
    
    @Autowired
    protected TestEntityManager entityManager;
    
    protected static final int TEST_USER_ID = 1;
    protected static final int ANOTHER_USER_ID = 2;
}
