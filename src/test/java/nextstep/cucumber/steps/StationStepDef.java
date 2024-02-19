package nextstep.cucumber.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java8.En;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.common.api.StationApiHelper;
import nextstep.cucumber.AcceptanceContext;
import nextstep.station.application.dto.StationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class StationStepDef implements En {
    @Autowired
    private AcceptanceContext context;

    public StationStepDef() {
        Given("지하철역들을 생성 요청하고", (final DataTable table) -> {
            final List<Map<String, String>> maps = table.asMaps();
            maps.forEach(params -> {
                final String stationName = params.get("name");
                final ExtractableResponse<Response> response = StationApiHelper.createStation(stationName);
                context.store.put(stationName, context.objectMapper.convertValue(response.jsonPath().get(), StationResponse.class));
            });
        });

        When("지하철역을 생성하면", () -> {
            context.response = StationApiHelper.createStation("강남역");
        });

        Then("지하철역이 생성된다", () -> {
            assertThat(context.response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        });

        Then("지하철역 목록 조회 시 생성한 역을 찾을 수 있다", () -> {
            final ExtractableResponse<Response> response = StationApiHelper.fetchStations();
            final List<String> stationNames = response.jsonPath().getList("name", String.class);
            assertThat(stationNames).containsAnyOf("강남역");
        });
    }

}
