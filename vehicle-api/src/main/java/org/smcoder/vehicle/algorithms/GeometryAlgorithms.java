package org.smcoder.vehicle.algorithms;

import org.smcoder.vehicle.algorithms.basic.Path;
import org.smcoder.vehicle.algorithms.basic.Point;
import org.smcoder.vehicle.algorithms.basic.Rectangle;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GeometryAlgorithms {
    public static List<Path> cutPathByRect(Path path, Rectangle rect) {
        List<Path> result = new ArrayList();
        Point currentPoint;
        int prePointIndex = -1;
        int currentPointIndex = 0;
        while ((currentPoint = path.getPoint(currentPointIndex)) != null) {
            switch (calculateStatus(prePointIndex, currentPointIndex, path, rect)) {
                case CUR_POT_IN_START:
                    result.add(createNewPath(currentPoint));
                    break;
                case CUR_POT_OUT_START:
                    break;
                case CUR_POT_IN_PRE_IN:
                    result.get(result.size() - 1).addPoints(currentPoint);
                    break;
                case CUR_POT_IN_PRE_OUT:
                    result.add(
                            createNewPath(
                                    calculateNewPoints(
                                            path.getPoint(prePointIndex),
                                            path.getPoint(currentPointIndex),
                                            rect,
                                            SegmentRectStatus.CUR_POT_IN_PRE_OUT)));
                    break;
                case CUR_POT_OUT_PRE_IN:
                    result.get(result.size() - 1).addPoints(
                            calculateNewPoints(
                                    path.getPoint(prePointIndex),
                                    path.getPoint(currentPointIndex),
                                    rect,
                                    SegmentRectStatus.CUR_POT_OUT_PRE_IN));
                    break;
                case CUR_POT_OUT_PRE_OUT:
                    break;
                default:
                    break;
            }
            prePointIndex++;
            currentPointIndex++;
        }
        return result;
    }

    private static List<Point> calculateNewPoints(Point pointStart, Point pointEnd, Rectangle rectangle, SegmentRectStatus pointPairStatus) {
        List<Point> result = new ArrayList();
        switch (pointPairStatus) {
            case CUR_POT_IN_START:
            case CUR_POT_IN_PRE_IN:
                result.add(pointEnd);
                break;
            case CUR_POT_OUT_PRE_OUT:
                result = calculateIntersectionPoints(pointStart, pointEnd, rectangle);
                break;
            case CUR_POT_IN_PRE_OUT:
                result = calculateIntersectionPoints(pointStart, pointEnd, rectangle);
                result.add(pointEnd);
                break;
            case CUR_POT_OUT_PRE_IN:
                result = calculateIntersectionPoints(pointStart, pointEnd, rectangle);
                break;
            case CUR_POT_OUT_START:
                break;
            default:
                break;
        }
        return result;
    }

    private static List<Point> calculateIntersectionPoints(Point pointStart, Point pointEnd, Rectangle rectangle) {
        List<Point> result = new ArrayList();
        double k;
        double b;
        if (pointStart.getX() == pointEnd.getX()) {
            k = Double.MAX_VALUE;
        } else {
            k = (pointEnd.getY() - pointStart.getY()) / (pointEnd.getX() - pointStart.getX());
        }
        if (k == Double.MAX_VALUE) {
            if (pointStart.getX() < rectangle.getRightX() && pointStart.getX() > rectangle.getLeftX()) {
                if (pointEnd.getY() > pointStart.getY()) {
                    if (rectangle.getTopY() <= pointStart.getY()) {
                    } else if (rectangle.getTopY() > pointStart.getY()
                            && rectangle.getTopY() <= pointEnd.getY()) {
                        if (pointStart.getY() >= rectangle.getBottomY()) {
                            result.add(new Point(pointStart.getX(), rectangle.getTopY()));
                        } else {
                            result.add(new Point(pointStart.getX(), rectangle.getBottomY()));
                            result.add(new Point(pointStart.getX(), rectangle.getTopY()));
                        }
                    } else if (rectangle.getTopY() > pointEnd.getY()) {
                        if (pointEnd.getY() > rectangle.getBottomY()) {
                            if (pointStart.getY() > rectangle.getBottomY()) {
                            } else {
                                result.add(new Point(pointStart.getX(), rectangle.getBottomY()));
                            }
                        } else {
                        }
                    }
                } else if (pointEnd.getY() < pointStart.getY()) {
                    if (rectangle.getBottomY() >= pointStart.getY()) {
                    } else if (rectangle.getBottomY() < pointStart.getY()
                            && rectangle.getBottomY() >= pointEnd.getY()) {
                        if (pointStart.getY() < rectangle.getTopY()) {
                            result.add(new Point(pointStart.getX(), rectangle.getBottomY()));
                        } else {
                            result.add(new Point(pointStart.getX(), rectangle.getTopY()));
                            result.add(new Point(pointStart.getX(), rectangle.getBottomY()));
                        }
                    } else if (rectangle.getBottomY() < pointEnd.getY()) {
                        if (pointEnd.getY() < rectangle.getTopY()) {
                            if (pointStart.getY() < rectangle.getTopY()) {
                            } else {
                                result.add(new Point(pointStart.getX(), rectangle.getTopY()));
                            }
                        } else {
                        }
                    }
                } else {
                }
            } else {
            }
        } else if (k == 0) {
            if (rectangle.getTopY() > pointStart.getY()
                    && rectangle.getBottomY() < pointStart.getY()) {
                if (pointStart.getX() < pointEnd.getX()) {
                    if (pointEnd.getX() <= rectangle.getLeftX()) {
                    } else if (pointEnd.getX() > rectangle.getLeftX() && pointEnd.getX() < rectangle.getRightX()) {
                        if (pointStart.getX() <= rectangle.getLeftX()) {
                            result.add(new Point(rectangle.getLeftX(), pointStart.getY()));
                        } else {
                        }
                    } else {
                        if (pointStart.getX() <= rectangle.getLeftX()) {
                            result.add(new Point(rectangle.getLeftX(), pointStart.getY()));
                            result.add(new Point(rectangle.getRightX(), pointStart.getY()));
                        } else if (pointStart.getX() > rectangle.getLeftX()
                                && pointStart.getX() < rectangle.getRightX()) {
                            result.add(new Point(rectangle.getRightX(), pointStart.getY()));
                        } else {
                        }
                    }
                } else if (pointStart.getX() > pointEnd.getX()) {
                    if (pointEnd.getX() >= rectangle.getRightX()) {
                    } else if (pointEnd.getX() < rectangle.getRightX()
                            && pointEnd.getX() > rectangle.getLeftX()) {
                        if (pointStart.getX() >= rectangle.getRightX()) {
                            result.add(new Point(rectangle.getRightX(), pointStart.getY()));
                        } else {
                        }
                    } else {
                        if (pointStart.getX() >= rectangle.getRightX()) {
                            result.add(new Point(rectangle.getRightX(), pointStart.getY()));
                            result.add(new Point(rectangle.getLeftX(), pointStart.getY()));
                        } else if (pointStart.getX() < rectangle.getRightX()
                                && pointStart.getX() > rectangle.getLeftX()) {
                            result.add(new Point(rectangle.getLeftX(), pointStart.getY()));
                        } else {
                        }
                    }
                } else {
                }
            } else {
            }
        } else {
            b = pointStart.getY() - k * pointStart.getX();
            result = new LinkedList<>();
            findInsectionPoints(pointStart, pointEnd, rectangle, rectangle.getLeftX(), k * rectangle.getLeftX() + b, result);
            findInsectionPoints(pointStart, pointEnd, rectangle, (rectangle.getBottomY() - b) / k, rectangle.getBottomY(), result);
            findInsectionPoints(pointStart, pointEnd, rectangle, rectangle.getRightX(), k * rectangle.getRightX() + b, result);
            findInsectionPoints(pointStart, pointEnd, rectangle, (rectangle.getTopY() - b) / k, rectangle.getTopY(), result);
            sortInsectionPoints(pointStart, pointEnd, result);
        }
        return result;
    }

    private static void sortInsectionPoints(Point pointStart, Point pointEnd, List<Point> tempInsectionPoints) {
        switch (tempInsectionPoints.size()) {
            case 0:
            case 1:
                break;
            case 2:
                if (Math.pow(tempInsectionPoints.get(0).getX() - pointStart.getX(), 2)
                        + Math.pow(tempInsectionPoints.get(0).getY() - pointStart.getY(), 2)
                        > Math.pow(tempInsectionPoints.get(1).getX() - pointStart.getX(), 2)
                        + Math.pow(tempInsectionPoints.get(1).getY() - pointStart.getY(), 2)) {
                    Point tempPot = tempInsectionPoints.get(0);
                    tempInsectionPoints.set(0, tempInsectionPoints.get(1));
                    tempInsectionPoints.set(1, tempPot);
                }
                break;
            default:
                break;
        }
    }

    private static void findInsectionPoints(Point pointStart, Point pointEnd, Rectangle rectangle, double pointX, double pointY, List<Point> tempInsectionPoints) {
        if (rectangle.checkPointInRect(pointX, pointY)
                && checkInPointPair(pointX, pointY, pointStart, pointEnd)) {
            tempInsectionPoints.add(new Point(pointX, pointY));
        }
    }

    private static boolean checkInPointPair(double pointX, double pointY, Point pointStart, Point pointEnd) {
        if ((pointX - pointStart.getX()) * (pointX - pointEnd.getX()) > 0) {
            return false;
        }
        if ((pointY - pointStart.getY()) * (pointY - pointEnd.getY()) > 0) {
            return false;
        }
        return true;
    }

    private static Path createNewPath(List<Point> currentPoints) {
        Path path = new Path();
        if (currentPoints != null) {
            for (Point point : currentPoints) {
                if (point != null) {
                    path.addPoints(point);
                }
            }
        }
        return path;
    }

    private static Path createNewPath(Point currentPoint) {
        Path path = new Path();
        if (currentPoint != null) {
            path.addPoints(currentPoint);
        }
        return path;
    }

    private static SegmentRectStatus calculateStatus(int prePointIndex,
                                                     int currentPointIndex,
                                                     Path path,
                                                     Rectangle rect) {
        Point prePoint = path.getPoint(prePointIndex);
        Point currentPoint = path.getPoint(currentPointIndex);
        SegmentRectStatus status = null;
        if (prePoint == null && rect.checkPointInRect(currentPoint)) {
            status = SegmentRectStatus.CUR_POT_IN_START;
        } else if (prePoint == null && !rect.checkPointInRect(currentPoint)) {
            status = SegmentRectStatus.CUR_POT_OUT_START;
        } else if (!rect.checkPointInRect(currentPoint) && rect.checkPointInRect(prePoint)) {
            status = SegmentRectStatus.CUR_POT_OUT_PRE_IN;
        } else if (!rect.checkPointInRect(currentPoint) && !rect.checkPointInRect(prePoint)) {
            status = SegmentRectStatus.CUR_POT_OUT_PRE_OUT;
        } else if (rect.checkPointInRect(prePoint)
                && rect.checkPointInRect(currentPoint)) {
            status = SegmentRectStatus.CUR_POT_IN_PRE_IN;
        } else if (rect.checkPointInRect(currentPoint)
                && !rect.checkPointInRect(prePoint)) {
            status = SegmentRectStatus.CUR_POT_IN_PRE_OUT;
        }
        return status;
    }

    private enum SegmentRectStatus {
        CUR_POT_OUT_START,
        CUR_POT_IN_START,
        CUR_POT_OUT_PRE_IN,
        CUR_POT_OUT_PRE_OUT,
        CUR_POT_IN_PRE_IN,
        CUR_POT_IN_PRE_OUT
    }
}
