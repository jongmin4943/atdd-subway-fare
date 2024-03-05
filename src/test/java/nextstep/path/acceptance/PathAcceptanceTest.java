package nextstep.path.acceptance;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.common.api.LineApiHelper;
import nextstep.common.api.PathApiHelper;
import nextstep.common.api.SectionApiHelper;
import nextstep.common.api.StationApiHelper;
import nextstep.core.AcceptanceTest;
import nextstep.core.RestAssuredHelper;
import nextstep.path.application.dto.PathResponse;
import nextstep.path.domain.PathType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@DisplayName("경로 관련 기능")
@AcceptanceTest
public class PathAcceptanceTest {

    private Long 강남역_Id;
    private Long 교대역_Id;
    private Long 양재역_Id;
    private Long 남부터미널역_Id;
    private Long 사당역_Id;
    private Long 서울역_Id;
    private Long 이호선_Id;
    private Long 신분당선_Id;
    private Long 삼호선_Id;
    private final int 교대역_강남역_distance = 5;
    private final int 교대역_강남역_duration = 4;
    private final int 강남역_양재역_distance = 10;
    private final int 강남역_양재역_duration = 4;
    private final int 교대역_남부터미널_distance = 2;
    private final int 교대역_남부터미널_duration = 3;
    private final int 남부터미널_양재역_distance = 3;
    private final int 남부터미널_양재역_duration = 2;


    /**
     * Given
     * (교대역--(5, 4)--강남역) - 2호선
     * (강남역--(10, 4)--양재역) - 신분당선
     * (교대역--(2, 3)--남부터미널역--(3, 2)--양재역) - 3호선
     * 을 생성하고
     */
    @BeforeEach
    void setUp() {
        // given
        교대역_Id = RestAssuredHelper.getIdFromBody(StationApiHelper.createStation("교대역"));
        강남역_Id = RestAssuredHelper.getIdFromBody(StationApiHelper.createStation("강남역"));
        양재역_Id = RestAssuredHelper.getIdFromBody(StationApiHelper.createStation("양재역"));
        남부터미널역_Id = RestAssuredHelper.getIdFromBody(StationApiHelper.createStation("남부터미널역_Id"));
        서울역_Id = RestAssuredHelper.getIdFromBody(StationApiHelper.createStation("서울역"));
        사당역_Id = RestAssuredHelper.getIdFromBody(StationApiHelper.createStation("사당역"));
        이호선_Id = RestAssuredHelper.getIdFromBody((LineApiHelper.createLine("2호선", "green", 교대역_Id, 강남역_Id, 교대역_강남역_distance, 교대역_강남역_duration, 0L)));
        신분당선_Id = RestAssuredHelper.getIdFromBody((LineApiHelper.createLine("신분당선", "red", 강남역_Id, 양재역_Id, 강남역_양재역_distance, 강남역_양재역_duration, 0L)));
        삼호선_Id = RestAssuredHelper.getIdFromBody((LineApiHelper.createLine("3호선", "orange", 교대역_Id, 남부터미널역_Id, 교대역_남부터미널_distance, 교대역_남부터미널_duration, 0L)));
        SectionApiHelper.createSection(삼호선_Id, 남부터미널역_Id, 양재역_Id, 남부터미널_양재역_distance, 남부터미널_양재역_duration);
    }


    /**
     * When 출발역(강남역)과 도착역(남부터미널)을 통해 경로를 조회하면
     * Then 최단거리인 강남역, 교대역, 남부터미널역의 경로와 거리 7, 소요시간 7이 반환된다.
     */
    @DisplayName("강남역 남부터미널역 사이 경로 조회시 강남역_교대역_남부터미널역 경로와 거리 7이 반환된다.")
    @Test
    void 강남역_남부터미널역_경로_조회_성공_테스트() {
        // when
        final ExtractableResponse<Response> response = 최소_길이_경로_조회_요청(강남역_Id, 남부터미널역_Id);

        // then
        경로가_반환된다(response, 교대역_강남역_distance + 교대역_남부터미널_distance, 교대역_강남역_duration + 교대역_남부터미널_duration, List.of(강남역_Id, 교대역_Id, 남부터미널역_Id));
    }

    /**
     * When 출발역과 도착역이 같은 역을 통해 경로를 조회하면
     * Then 에러가 난다.
     */
    @DisplayName("출발역과 도착역이 같은 역을 통해 경로를 조회하면 에러가 난다.")
    @Test
    void 같은_출발역_도착역_경로_조회_테스트() {
        // when
        final ExtractableResponse<Response> response = 최소_길이_경로_조회_요청(남부터미널역_Id, 남부터미널역_Id);

        // then
        요청에_실패_한다(response);
    }

    /**
     * When 존재하지 않는 출발역이나 도착역을 통해 경로를 조회하면
     * Then 에러가 난다.
     */
    @DisplayName("존재하지 않는 출발역이나 도착역을 통해 경로를 조회하면 에러가 난다.")
    @Test
    void 존재하지않는_출발역_경로_조회_테스트() {
        // when
        final ExtractableResponse<Response> response = 최소_길이_경로_조회_요청(사당역_Id, 남부터미널역_Id);

        // then
        경로찾기에_실패_한다(response);
    }

    /**
     * Given (사당역--5--서울역) - 4호선 을 생성하고
     * When 연결되지 않은 출발역(교대역)과 도착역(서울역)을 통해 경로를 조회하면
     * Then 에러가 난다.
     */
    @DisplayName("연결되지 않은 출발역과 도착역을 통해 경로를 조회하면 에러가 난다.")
    @Test
    void 연결되지_않은_출발역_도착역_경로_조회_테스트() {
        // when
        final ExtractableResponse<Response> response = 최소_길이_경로_조회_요청(교대역_Id, 서울역_Id);

        // then
        경로찾기에_실패_한다(response);
    }


    /**
     * When 출발역(강남역)과 도착역(남부터미널)까지의 최소 시간 기준으로 경로 조회를 요청하면
     * Then 최소 시간 기준 경로인 강남역, 교대역, 남부터미널역의 경로와 거리 13, 소요시간 6 이 반환된다.
     */
    @DisplayName("강남역 남부터미널역 까지의 최소 시간 기준으로 경로 조회 강남역_양재역_남부터미널역 경로와 거리 13, 소요시간 6이 반환된다.")
    @Test
    void 강남역_남부터미널역_최소시간_경로_조회_성공_테스트() {
        // when
        final ExtractableResponse<Response> response = 최소_시간_경로_조회_요청(강남역_Id, 남부터미널역_Id);

        // then
        경로가_반환된다(response, 강남역_양재역_distance + 남부터미널_양재역_distance, 강남역_양재역_duration + 남부터미널_양재역_duration, List.of(강남역_Id, 양재역_Id, 남부터미널역_Id));
    }

    private void 경로가_반환된다(final ExtractableResponse<Response> response, final int distance, final int duration, final List<Long> stationIds) {
        assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
            final PathResponse pathResponse = response.as(PathResponse.class);
            softly.assertThat(pathResponse.getDistance()).isEqualTo(distance);
            softly.assertThat(pathResponse.getDuration()).isEqualTo(duration);
            softly.assertThat(pathResponse.getStations()).extracting("id").containsExactlyElementsOf(stationIds);
        });
    }

    private ExtractableResponse<Response> 최소_길이_경로_조회_요청(final Long startStationId, final Long endStationId) {
        return PathApiHelper.findPath(startStationId, endStationId, PathType.DISTANCE);
    }

    private ExtractableResponse<Response> 최소_시간_경로_조회_요청(final Long startStationId, final Long endStationId) {
        return PathApiHelper.findPath(startStationId, endStationId, PathType.DURATION);
    }

    private void 경로찾기에_실패_한다(final ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    private void 요청에_실패_한다(final ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }


}
