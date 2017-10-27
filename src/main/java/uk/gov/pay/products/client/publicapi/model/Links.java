package uk.gov.pay.products.client.publicapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Links {

    private Link self;
    private Link nextUrl;
    private Link nextUrlPost;
    private Link events;
    private Link refunds;
    private Link cancel;

    public Links() {
    }

    public Links(
            @JsonProperty("self") Link self,
            @JsonProperty("next_url") Link nextUrl,
            @JsonProperty("next_url_post") Link nextUrlPost,
            @JsonProperty("events") Link events,
            @JsonProperty("refunds") Link refunds,
            @JsonProperty("cancel") Link cancel) {
        this.self = self;
        this.nextUrl = nextUrl;
        this.nextUrlPost = nextUrlPost;
        this.events = events;
        this.refunds = refunds;
        this.cancel = cancel;
    }

    public Link getSelf() {
        return self;
    }

    public void setSelf(Link self) {
        this.self = self;
    }

    public Link getNextUrl() {
        return nextUrl;
    }

    public void setNextUrl(Link nextUrl) {
        this.nextUrl = nextUrl;
    }

    public Link getNextUrlPost() {
        return nextUrlPost;
    }

    public void setNextUrlPost(Link nextUrlPost) {
        this.nextUrlPost = nextUrlPost;
    }

    public Link getEvents() {
        return events;
    }

    public void setEvents(Link events) {
        this.events = events;
    }

    public Link getRefunds() {
        return refunds;
    }

    public void setRefunds(Link refunds) {
        this.refunds = refunds;
    }

    public Link getCancel() {
        return cancel;
    }

    public void setCancel(Link cancel) {
        this.cancel = cancel;
    }

    @Override
    public String toString() {
        return "Links{" +
                "self=" + self +
                ", nextUrl=" + nextUrl +
                ", nextUrlPost=" + nextUrlPost +
                ", events=" + events +
                ", refunds=" + refunds +
                ", cancel=" + cancel +
                '}';
    }
}
