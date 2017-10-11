package fluxoid.fit;

public enum FileType {
    DEVICE(1),
    SETTINGS(2),
    SPORT(3),
    ACTIVITY(4),
    WORKOUT(5),
    COURSE(6),
    SCHEDULE(7),
    WEIGHT(9),
    TOTALS(10),
    GOALS(11),
    BLOOD_PRESSURE(14),
    ACTIVITY_SUMMARY(20);

    private final int id;

    FileType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}


