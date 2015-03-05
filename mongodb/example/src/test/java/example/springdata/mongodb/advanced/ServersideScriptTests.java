/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package example.springdata.mongodb.advanced;

import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsNull.*;
import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.script.ExecutableMongoScript;
import org.springframework.data.mongodb.core.script.NamedMongoScript;
import org.springframework.data.util.Version;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongodb.BasicDBObject;

import example.springdata.mongodb.customer.Customer;
import example.springdata.mongodb.util.RequiresMongoDB;

/**
 * @author Christoph Strobl
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ApplicationConfiguration.class)
public class ServersideScriptTests {

	@ClassRule public static RequiresMongoDB mongodbAvailable = RequiresMongoDB.atLeast(new Version(2, 6));

	@Autowired AdvancedRepository repository;
	@Autowired MongoOperations operations;

	@Before
	public void setUp() {

		// just make sure we remove everything properly
		operations.getCollection("system.js").remove(new BasicDBObject());
		repository.deleteAll();
	}

	/**
	 * Use a script execution to create an atomic put-if-absent operation that fulfills the contract of
	 * {@link Map#putIfAbsent(Object, Object)}
	 */
	@Test
	public void putIfAbsent() {

		Customer ned = new Customer("Ned", "Stark");
		ned.setId("ned-stark");

		// #1: on first insert null has to be returned
		assertThat(repository.putIfAbsent(ned), nullValue());

		// #2: change the firstname and put again, we expect to the existing customer without the change
		ned.setFirstname("Eddard");
		assertThat(repository.putIfAbsent(ned).getFirstname(), is("Ned"));

		// #3: make sure the entity has not been altered by #2
		assertThat(repository.findOne(ned.getId()).getFirstname(), is("Ned"));
	}

	/**
	 * Store and call an arbitary JavaScript function (in this case a simple echo script) via its name.
	 */
	@Test
	public void simpleScriptExecution() {

		operations.scriptOps().register(
				new NamedMongoScript("echoScript", new ExecutableMongoScript("function(x) { return x; }")));

		Object o = operations.scriptOps().call("echoScript", "Hello echo...!");
		assertThat(o, is((Object) "Hello echo...!"));
	}
}
