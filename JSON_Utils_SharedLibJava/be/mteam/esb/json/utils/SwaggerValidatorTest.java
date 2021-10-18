package be.mteam.esb.json.utils;



import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bjansen.ssv.SwaggerValidator;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;


public class SwaggerValidatorTest {

	public SwaggerValidatorTest(){

	}

	public void validate() throws Exception {

		String jsonSchema = "swagger.json";

		ClassLoader classLoader = this.getClass().getClassLoader();
		URL url = classLoader.getResource(jsonSchema);

		InputStream jSonSchemaStream = url.openStream();
		Reader jSonSchemaReader = new InputStreamReader(jSonSchemaStream);
		SwaggerValidator validator = SwaggerValidator
				.forJsonSchema(jSonSchemaReader);

		String jSonMessage = "[  {\"siteNumber\": 334,  \"cutoverNumber\": 53052,  \"cutoverMoment\": \"2000-05-07T22:44:21.885Z\",  \"cutoverType\": \"DAILY\",  \"nozzleIndexes\": [    {      \"nozzleNumber\": 50,      \"product\": \"ce\",      \"tankGroup\": 97,      \"hardwareMeasurement\": {        \"amount\": 12345678912390.57488274,        \"volume\": 16.60304269      },      \"softwareMeasurement\": {        \"amount\": 25.23974972,        \"volume\": 57.02394999      },      \"currency\": \"PAB\"    }  ],  \"mopsList\": [    {      \"terminalType\": \"h\",      \"terminalTypeId\": 98,      \"terminalId\": 999998,      \"mopConversionId\": 99998,      \"terminalCutoverNbr\": 999998,      \"productType\": \"SHOP\",      \"volume\": 35.53737132,      \"amount\": 31.04580585,      \"currency\": \"HTG\"    }  ],  \"shopSales\": [    {      \"department\": 97,      \"subDepartment\": 98,      \"description\": \"Awbibba.\",      \"amount\": 19.6399254,      \"quantity\": 72.45259939,      \"returnAmount\": 97.94715206,      \"returnQuantity\": 90.2623483,      \"eftCode1\": \"l\",      \"eftCode2\": \"h\",      \"vatRate\": 21.98071116,      \"vatPercentage\": 72.77182415    }  ],  \"dippings\": [    {      \"tankNumber\": 2,      \"tankGroup\": 98,      \"tankCapacity\": 49.24866152,      \"productMeasurement\": {        \"product\": \"do\",        \"waterHeight\": 49.59417216,        \"waterVolume\": 8.82983275,        \"productHeight\": 43.42144658,        \"productVolume\": 46.71371079,        \"temperature\": 997,        \"gaugeStatus\": \"NOK\"      }    }  ]}]";

		// load the jSon root element
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonRootNode = objectMapper.readValue(jSonMessage,
				JsonNode.class);


		JsonNode jsonFirstElementNode = jsonRootNode.elements().next();
		ProcessingReport report = validator.validate(jsonFirstElementNode,
				"/definitions/Cutover",true);


		if (!(report.isSuccess())) {
			String errorMessage = "jSon validation error : ";
			JsonNode error;
			int nbrOfErrors = 0;
			for (ProcessingMessage processingMessage : report) {
				if (nbrOfErrors > 0) {
					errorMessage = errorMessage + " # ";
				}
				error = processingMessage.asJson();

				errorMessage = errorMessage + error.findValue("pointer")
						+ " " + error.findValue("message");
				nbrOfErrors = +1;
			}

			System.out.println(errorMessage);

		}

	}
	
	public static void main(String[] args) throws Exception{

	
		
		SwaggerValidatorTest swaggerValidatorTest = new SwaggerValidatorTest();
		swaggerValidatorTest.validate();
		

	}


}
