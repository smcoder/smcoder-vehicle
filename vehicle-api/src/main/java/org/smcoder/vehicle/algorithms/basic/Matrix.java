package org.smcoder.vehicle.algorithms.basic;

public class Matrix {
    private int row;
    private int col;
    private double[][] array;

    public double[][] getArray() {
        return array;
    }

    public void setArray(double[][] array) {
        int row = array.length;
        int col = array[0].length;
        for (int i = 1; i < row; i++) {
            if (col != array[i].length) {
                return;
            }
        }
        this.row = row;
        this.col = col;
        this.array = array;
    }

    @Override
    public String toString() {
        if (array == null) {
            return "\r\n";
        }
        String result = "";
        row = array.length;
        col = array[row - 1].length;

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                result += array[i][j] + " ";
            }
            result += "\r\n";
        }
        return result;
    }

    /**
     * 传入一个矩阵类进行相乘
     */
    public Matrix multiply(Matrix x) {
        Matrix m = new Matrix();
        m.setArray(multiply(x.getArray()));
        return m;

    }

    /**
     * 传入一个矩阵类进行相乘
     */
    public double[][] multiply(double[][] aim) {
        if (this.col != aim.length) {
            return null;
        }
        double[][] result = new double[this.row][aim[0].length];
        for (int row = 0; row < this.row; row++) {
            for (int col = 0; col < aim[0].length; col++) {
                double num = 0;
                for (int i = 0; i < this.col; i++) {
                    num += array[row][i] * aim[i][col];
                }
                result[row][col] = num;
            }
        }
        return result;
    }
}
