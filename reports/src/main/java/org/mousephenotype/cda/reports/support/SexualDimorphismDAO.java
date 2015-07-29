/*******************************************************************************
 * Copyright 2015 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this targetFile except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 *******************************************************************************/
package org.mousephenotype.cda.reports.support;

import org.mousephenotype.cda.db.dao.HibernateDAO;

import java.util.List;

public interface SexualDimorphismDAO extends HibernateDAO {
	
	List<String[]> sexualDimorphismReportNoBodyWeight(String baseUrl) ;
	
	List<String[]> sexualDimorphismReportWithBodyWeight(String baseUrl);
	
}
