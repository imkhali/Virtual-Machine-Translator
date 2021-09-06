package model;

import java.util.List;

public record Command(String string, CommandType type, List<String> tokens) {
}