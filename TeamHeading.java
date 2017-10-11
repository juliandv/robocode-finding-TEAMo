package comp771;

public class TeamHeading implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private double heading = 0.0;

    public TeamHeading(double heading) {
        this.heading = heading;
    }

    public double getTeamHeading() {
        return heading;
    }
}