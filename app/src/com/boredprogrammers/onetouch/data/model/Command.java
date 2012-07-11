package com.boredprogrammers.onetouch.data.model;

import java.util.List;

public final class Command {
    public String shortName;
    public String title;
    public String description;
    public List<CommandLine> exec;

    @Override
    public String toString() {
        return "Command [shortName=" + shortName + ", title=" + title + ", description=" + description + ", exec=" + exec + "]";
    }

}
