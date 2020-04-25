package NASA.utils;

import NASA.model.enums.QueryParameterType;
import NASA.model.exceptions.EONETException;
import lombok.Data;

import java.util.Map;

@Data
public class URLManipulator {
    private String url;
    private Map<QueryParameterType, String> queryParams;

    public URLManipulator(String url, Map<QueryParameterType, String> queryParams) {
        this.url = url;
        if (queryParams == null || queryParams.isEmpty()) {
            throw new EONETException("You must provide query parameters for using this instance.");
        }
        this.queryParams = queryParams;
    }

    public String getUrlWithQueryParams() {
        URL.URLAddQueriesStep addQueriesStep = URL.builder().prepareForQueries(url);
        int queryParamsNo = queryParams.entrySet().size();
        int i = 0;
        for (Map.Entry<QueryParameterType, String> queryParam : queryParams.entrySet()) {
            if (i < queryParamsNo - 1) {
                addQueriesStep.addQueryParam(queryParam.getKey(), queryParam.getValue());
                i++;
            } else {
                return addQueriesStep.addLastQueryParam(queryParam.getKey(), queryParam.getValue()).build().getUrl();
            }
        }
        return null;
    }
}
