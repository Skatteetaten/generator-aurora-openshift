package <%=packageName%>.controllers

import org.junit.Rule
import org.springframework.restdocs.JUnitRestDocumentation
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.snippet.Snippet
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders

import spock.lang.Specification

abstract class AbstractControllerTest extends Specification {

  @Rule
  def JUnitRestDocumentation jUnitRestDocumentation = new JUnitRestDocumentation("target/generated-snippets");

  MockMvc mockMvc

  def setup() {

    def controllers = []
    controllers.addAll(controllersUnderTest)
    mockMvc = MockMvcBuilders.standaloneSetup(controllers.toArray())
        .setControllerAdvice(new ErrorHandler())
        .apply(MockMvcRestDocumentation.documentationConfiguration(jUnitRestDocumentation))
        .build()

  }

  protected static RestDocumentationResultHandler prettyDoc(String identifier, Snippet... snippets) {
    MockMvcRestDocumentation.
        document(identifier, Preprocessors.preprocessRequest(Preprocessors.prettyPrint()), Preprocessors.
            preprocessResponse(Preprocessors.prettyPrint()), snippets)
  }

  protected abstract List<Object> getControllersUnderTest()
}
