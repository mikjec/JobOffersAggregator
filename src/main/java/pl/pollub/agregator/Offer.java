package pl.pollub.agregator;

public class Offer {

    private String title;
    private String skills;
    private String location;

    public Offer(String title, String skills, String location) {
        if (isInvalid(title) || isInvalid(skills) || isInvalid(location)) {
            throw new IllegalArgumentException(
                    "Brakujace lub puste pole w ofercie (title='" + title +
                    "', skills='" + skills + "', location='" + location + "')"
            );
        }
        this.title = title.trim();
        this.skills = skills.trim();
        this.location = location.trim();
    }

    private boolean isInvalid(String value) {
        return value == null || value.trim().isEmpty();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (isInvalid(title)) {
            throw new IllegalArgumentException("Pole 'title' nie moze byc puste.");
        }
        this.title = title.trim();
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        if (isInvalid(skills)) {
            throw new IllegalArgumentException("Pole 'skills' nie moze byc puste.");
        }
        this.skills = skills.trim();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        if (isInvalid(location)) {
            throw new IllegalArgumentException("Pole 'location' nie moze byc puste.");
        }
        this.location = location.trim();
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s", title, skills, location);
    }
}
