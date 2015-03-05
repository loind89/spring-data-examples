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

import java.util.Map;

import example.springdata.mongodb.customer.Customer;

/**
 * @author Christoph Strobl
 */
public interface AdvancedRepositoryCustom {

	/**
	 * If the specified customer is not already persisted in MongoDB (or is mapped to {@code null}) associates it with the
	 * given value and returns {@code null}, else returns the current value.
	 * 
	 * @see Map#putIfAbsent(Object, Object)
	 * @param customer
	 * @return {@literal null} when inserted otherwise existing {@link Customer}
	 */
	Customer putIfAbsent(Customer customer);

}
