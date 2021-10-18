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
import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbOutputTerminal;
import com.ibm.broker.plugin.MbUserException;

public class JSonObjectValidator extends MbJavaComputeNode {

	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");
		MbOutputTerminal alt = getOutputTerminal("alternate");

		MbMessage inMessage = inAssembly.getMessage();
		MbMessageAssembly outAssembly = null;
		try {
			// create new message as a copy of the input
			MbMessage outMessage = new MbMessage(inMessage);
			outAssembly = new MbMessageAssembly(inAssembly, outMessage);

			// retrieve variables
			MbElement envVariablesElement = inAssembly.getGlobalEnvironment()
					.getRootElement().getFirstElementByPath("Variables");
			MbElement jsonSchemaElement = envVariablesElement
					.getFirstElementByPath("jSonObjectValidator/jSonSchema");

			MbElement jSonPointerElement = envVariablesElement
					.getFirstElementByPath("jSonObjectValidator/jSonPointer");

			if (jsonSchemaElement == null) {
				throw new MbUserException(this, "evaluate()", "jSon", "500",
						"No jSonSchema variable found in JSonObjectValidator",
						null);
			}

			if (jSonPointerElement == null) {
				throw new MbUserException(this, "evaluate()", "jSon", "500",
						"No jSonPointer variable found in JSonObjectValidator",
						null);
			}

			// load the jSon schema
			ClassLoader classLoader = this.getClass().getClassLoader();
			URL url = classLoader.getResource(jsonSchemaElement
					.getValueAsString());

			InputStream jSonSchemaStream = url.openStream();
			Reader jSonSchemaReader = new InputStreamReader(jSonSchemaStream);
			SwaggerValidator validator = SwaggerValidator
					.forJsonSchema(jSonSchemaReader);

			// load the jSon from message
			MbElement jSonMessageElement = inMessage.getRootElement()
					.getFirstElementByPath("JSON/Data/payload");
			 

			String jSonMessage = jSonMessageElement.getValueAsString();

			// load the jSon root element
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonRootNode = objectMapper.readValue(jSonMessage,
					JsonNode.class);

			if (!jsonRootNode.isObject()) {
				throw new MbUserException(this, "evaluate()", "jSon", "500",
						"jSon root element is not an object", null);
			}

			JsonNode jsonFirstElementNode = jsonRootNode.elements().next();
			ProcessingReport report = validator.validate(jsonFirstElementNode,
					"/properties/Car");
			 

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

				throw new MbUserException(this, "evaluate()", "jSon", "500",
						errorMessage, null);
			}

			// TODO adapt exceptions

		} catch (MbException e) {
			// Re-throw to allow Broker handling of MbException
			throw e;
		} catch (RuntimeException e) {
			// Re-throw to allow Broker handling of RuntimeException
			throw e;
		} catch (Exception e) {
			// Consider replacing Exception with type(s) thrown by user code
			// Example handling ensures all exceptions are re-thrown to be
			// handled in the flow
			throw new MbUserException(this, "evaluate()", "jSon", "500",
					e.toString(), null);
		}
		// The following should only be changed
		// if not propagating message to the 'out' terminal
		out.propagate(outAssembly);

	}
	
	

}
