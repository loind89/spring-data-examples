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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.script.ExecutableMongoScript;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import example.springdata.mongodb.customer.Customer;

/**
 * @author Christoph Strobl
 */
public class AdvancedRepositoryImpl implements AdvancedRepositoryCustom {

	@Autowired MongoOperations mongoOps;

	/*
	 * (non-Javadoc)
	 * @see example.springdata.mongodb.advanced.AdvancedRepositoryCustom#putIfAbsent(example.springdata.mongodb.customer.Customer)
	 */
	@Override
	public Customer putIfAbsent(Customer customer) {

		if (!mongoOps.collectionExists(Customer.class)) {
			mongoOps.createCollection(Customer.class);
		}

		Object result = mongoOps.scriptOps().execute(createScript(customer));
		if (result == null) {
			return null;
		}

		return mongoOps.getConverter().read(Customer.class, (DBObject) result);
	}

	private ExecutableMongoScript createScript(Object entity) {

		String collectionName = mongoOps.getCollectionName(Customer.class);
		Object id = mongoOps.getConverter().getMappingContext().getPersistentEntity(Customer.class)
				.getIdentifierAccessor(entity).getIdentifier();

		DBObject dbo = new BasicDBObject();
		mongoOps.getConverter().write(entity, dbo);

		String scriptString = String
				.format(
						"object  =  db.%1$s.findOne('{\"_id\": \"%2$s\"}'); if(object == null) { db.%1s.insert(%3$s); return null; } else { return object; }",
						collectionName, id, dbo);

		return new ExecutableMongoScript(scriptString);
	}
}
