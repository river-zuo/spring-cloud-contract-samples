package com.example;

import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.contract.stubrunner.junit.StubRunnerRule;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.StringUtils;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Marcin Grzejszczak
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ClientApplication.class, webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class BeerControllerWebTestClientTest extends AbstractTest {

	@Autowired MockMvc mockMvc;
	@Autowired BeerController beerController;

	//remove::start[]
	@Rule
	public StubRunnerRule rule = new StubRunnerRule()
			.downloadStub("com.example","beer-api-producer-webtestclient-restdocs")
			.stubsMode(StubRunnerProperties.StubsMode.LOCAL);
	//remove::end[]

	@BeforeClass
	public static void beforeClass() {
		Assume.assumeTrue("Spring Cloud Contract must be in version at least 2.1.0", atLeast210());
		Assume.assumeTrue("Env var OLD_PRODUCER_TRAIN must not be set", StringUtils.isEmpty(System.getenv("OLD_PRODUCER_TRAIN")));
	}

	@Before
	public void setupPort() {
		// remove::start[]
		this.beerController.port = this.rule.findStubUrl("beer-api-producer-webtestclient-restdocs").getPort();
		// remove::end[]
	}

	private static boolean atLeast210() {
		try {
			Class.forName("org.springframework.cloud.contract.verifier.util.ContractVerifierUtil");
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	@Test public void should_give_me_a_beer_when_im_old_enough() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders.post("/beer")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.json.write(new Person("marcin", 22)).getJson()))
				.andExpect(status().isOk())
				.andExpect(content().string("THERE YOU GO"));
	}

	@Test public void should_reject_a_beer_when_im_too_young() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders.post("/beer")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.json.write(new Person("marcin", 17)).getJson()))
				.andExpect(status().isOk())
				.andExpect(content().string("GET LOST"));
	}
}