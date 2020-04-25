package NASA.utils;

import NASA.model.enums.EventStatus;
import NASA.model.enums.QueryParameterType;

import java.util.Objects;

public class URL {
    private String url;

    private URL(Builder builder) {
        this.url = builder.url;
    }

    public String getUrl() {
        return url;
    }

    public static URLPrepareForQueriesStep builder() {
        return new Builder();
    }

    public interface URLPrepareForQueriesStep {
        URLAddQueriesStep prepareForQueries(String url);
    }

    public interface URLAddQueriesStep {
        URLAddQueriesStep addQueryParam(QueryParameterType key, String value);

        Build addLastQueryParam(QueryParameterType key, String value);
    }

    public interface Build {
        URL build();
    }

    public static class Builder implements URLPrepareForQueriesStep, URLAddQueriesStep, Build {
        private String url;

        @Override
        public URLAddQueriesStep prepareForQueries(String url) {
            Objects.requireNonNull(url);
            this.url = url + "?";
            return this;
        }

        @Override
        public URLAddQueriesStep addQueryParam(QueryParameterType key, String value) {
            Objects.requireNonNull(url);
            Objects.requireNonNull(key);
            Objects.requireNonNull(value);
            this.url += key + "=" + value + "&";
            return this;
        }

        @Override
        public Build addLastQueryParam(QueryParameterType key, String value) {
            Objects.requireNonNull(url);
            Objects.requireNonNull(key);
            Objects.requireNonNull(value);
            this.url += key + "=" + value;
            return this;
        }

        @Override
        public URL build() {
            return new URL(this);
        }
    }
}
