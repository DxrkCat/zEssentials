package fr.maxlego08.essentials.api.discord;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record DiscordEmbedConfiguration(String title, String description, String url, Color color, Footer footer,
                                        Thumbnail thumbnail,
                                        Image image, Author author, List<Field> fields) {

    public static List<DiscordEmbedConfiguration> convertToEmbedObjects(List<Map<?, ?>> data) {
        List<DiscordEmbedConfiguration> embedObjects = new ArrayList<>();

        for (Map<?, ?> map : data) {
            String title = getString(map, "title");
            String description = getString(map, "description");
            String url = getString(map, "url");
            Color color = hexToColor(getString(map, "color"));

            DiscordEmbedConfiguration.Footer footer = null;
            Map<?, ?> footerMap = getMap(map, "footer");
            if (footerMap != null) {
                String footerText = getString(footerMap, "text");
                String footerIconUrl = getString(footerMap, "icon-url");
                footer = new DiscordEmbedConfiguration.Footer(footerText, footerIconUrl);
            }

            DiscordEmbedConfiguration.Thumbnail thumbnail = null;
            Map<?, ?> thumbnailMap = getMap(map, "thumbnail");
            if (thumbnailMap != null) {
                String thumbnailUrl = getString(thumbnailMap, "url");
                thumbnail = new DiscordEmbedConfiguration.Thumbnail(thumbnailUrl);
            }

            DiscordEmbedConfiguration.Image image = null;
            Map<?, ?> imageMap = getMap(map, "image");
            if (imageMap != null) {
                String imageUrl = getString(imageMap, "url");
                image = new DiscordEmbedConfiguration.Image(imageUrl);
            }

            DiscordEmbedConfiguration.Author author = null;
            Map<?, ?> authorMap = getMap(map, "author");
            if (authorMap != null) {
                String authorName = getString(authorMap, "name");
                String authorUrl = getString(authorMap, "url");
                String authorIconUrl = getString(authorMap, "icon-url");
                author = new DiscordEmbedConfiguration.Author(authorName, authorUrl, authorIconUrl);
            }

            List<DiscordEmbedConfiguration.Field> fields = new ArrayList<>();
            List<Map<String, Object>> fieldsList = getList(map, "fields");
            if (fieldsList != null) {
                for (Map<String, Object> fieldMap : fieldsList) {
                    String fieldName = getString(fieldMap, "name");
                    String fieldValue = getString(fieldMap, "value");
                    Boolean inline = getBoolean(fieldMap, "inline");
                    fields.add(new DiscordEmbedConfiguration.Field(fieldName, fieldValue, inline != null ? inline : false));
                }
            }

            DiscordEmbedConfiguration embedObject = new DiscordEmbedConfiguration(title, description, url, color, footer, thumbnail, image, author, fields);

            embedObjects.add(embedObject);
        }

        System.out.println("Résult: " + embedObjects);

        return embedObjects;
    }

    private static String getString(Map<?, ?> map, String key) {
        Object value = map.get(key);
        return value instanceof String ? (String) value : null;
    }

    private static Map<?, ?> getMap(Map<?, ?> map, String key) {
        Object value = map.get(key);
        return value instanceof Map<?, ?> ? (Map<?, ?>) value : null;
    }

    private static List<Map<String, Object>> getList(Map<?, ?> map, String key) {
        Object value = map.get(key);
        return value instanceof List<?> ? (List<Map<String, Object>>) value : null;
    }

    private static Boolean getBoolean(Map<?, ?> map, String key) {
        Object value = map.get(key);
        return value instanceof Boolean ? (Boolean) value : null;
    }

    private static Color hexToColor(String hex) {

        if (hex == null) return null;

        // Retire le caractère '#' s'il est présent
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        // Vérifie que la chaîne de caractères contient bien 6 ou 8 caractères hexadécimaux
        if (hex.length() != 6 && hex.length() != 8) {
            throw new IllegalArgumentException("Invalid hex color string");
        }

        // Parse les composants RGB ou RGBA
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        int a = 255; // Opacité par défaut

        if (hex.length() == 8) {
            a = Integer.parseInt(hex.substring(6, 8), 16);
        }

        return new Color(r, g, b, a);
    }

    public void apply(DiscordWebhook discordWebhook, String playerName, UUID playerUuid, String message) {

        DiscordWebhook.EmbedObject embedObject = new DiscordWebhook.EmbedObject();

        embedObject.setAuthor(
                replace(this.author.name, playerName, message, playerUuid),
                replace(this.author.url, playerName, message, playerUuid),
                replace(this.author.iconUrl, playerName, message, playerUuid)
        );

        if (this.description != null) {
            embedObject.setDescription(replace(this.description, playerName, message, playerUuid));
        }

        if (this.color != null) {
            embedObject.setColor(this.color);
        }

        if (this.title != null) {
            embedObject.setTitle(replace(this.title, playerName, message, playerUuid));
        }

        discordWebhook.addEmbed(embedObject);
    }

    private String replace(String value, String playerName, String message, UUID uuid) {
        return value == null ? "" : value.replace("%player%", playerName).replace("%message%", message == null ? "" : message).replace("%uuid%", uuid.toString());
    }

    public record Footer(String text, String iconUrl) {
    }

    public record Thumbnail(String url) {
    }

    public record Image(String url) {
    }

    public record Author(String name, String url, String iconUrl) {
    }

    public record Field(String name, String value, boolean inline) {
    }

}