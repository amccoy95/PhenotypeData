/*******************************************************************************
 * Copyright © 2018 EMBL - European Bioinformatics Institute
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 ******************************************************************************/

package org.mousephenotype.impress.wsdlclients;

import org.mousephenotype.impress.GetParameterOntologyOptions;
import org.mousephenotype.impress.GetParameterOntologyOptionsResponse;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

public class ParameterOntologyOptionsClient extends WebServiceGatewaySupport {

    public GetParameterOntologyOptionsResponse getParameterOntologyOptions(String parameterKey) {

        GetParameterOntologyOptions request = new GetParameterOntologyOptions();
        request.setParameterKey(parameterKey);

        GetParameterOntologyOptionsResponse response = (GetParameterOntologyOptionsResponse) getWebServiceTemplate()
                .marshalSendAndReceive("https://www.mousephenotype.org/impress/soap/server",
                                       request,
                                       new SoapActionCallback("https://www.mousephenotype.org/impress/soap/server/GetParameterOntologyOptions"));

        return response;
    }
}