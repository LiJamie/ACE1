package edu.ntut.selab;

import edu.ntut.selab.data.Point;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class PointerHelperTest {
    private Point upperLeftPoint, lowerRightPoint;
    private final int OFFSET = 1;

    @Before
    public void setup() {
        this.upperLeftPoint = new Point(0, 0);
        this.lowerRightPoint = new Point(320, 320);
    }

    @Test
    public void testGetAverage() {
        int a = 1, b = 2;
        assertEquals(1, PointHelper.getAverage(a, b));
    }

    @Test
    public void testGetCenterPoint() {
        Point centerPoint = PointHelper.getCenterPoint(this.upperLeftPoint, this.lowerRightPoint);
        assertEquals(160, centerPoint.x());
        assertEquals(160, centerPoint.y());
    }
}
