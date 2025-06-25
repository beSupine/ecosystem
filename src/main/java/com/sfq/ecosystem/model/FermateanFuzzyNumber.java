package com.sfq.ecosystem.model;

import java.util.Objects;

/**
 * 费马模糊数 (Fermatean Fuzzy Number - FFN) 模型类
 * <p>
 * 用于表示一个区间值的费马模糊集，包含隶属度 (alpha) 和非隶属度 (beta) 两个区间。
 * 同时实现了费马模糊数的相关运算法则和比较方法。
 * 该类实现了 Comparable 接口，以便于进行排序和比较。
 */
public class FermateanFuzzyNumber implements Comparable<FermateanFuzzyNumber> {

    // 定义一个极小值，用于浮点数比较，避免精度问题
    private static final double EPSILON = 1e-9;

    // 隶属度区间的下界
    private double alphaL;
    // 隶属度区间的上界
    private double alphaU;
    // 非隶属度区间的下界
    private double betaL;
    // 非隶属度区间的上界
    private double betaU;

    // 预定义的常量：模糊零
    public static final FermateanFuzzyNumber FFN_ZERO = new FermateanFuzzyNumber(new double[]{0.0, 0.0}, new double[]{1.0, 1.0});
    // 预定义的常量：模糊一
    public static final FermateanFuzzyNumber FFN_ONE = new FermateanFuzzyNumber(new double[]{1.0, 1.0}, new double[]{0.0, 0.0});

    /**
     * 构造函数
     *
     * @param alphaInterval 隶属度区间 [alpha_L, alpha_U]
     * @param betaInterval  非隶属度区间 [beta_L, beta_U]
     */
    public FermateanFuzzyNumber(double[] alphaInterval, double[] betaInterval) {
        this.alphaL = alphaInterval[0];
        this.alphaU = alphaInterval[1];
        this.betaL = betaInterval[0];
        this.betaU = betaInterval[1];

        // 基本的合法性校验
//        if (!(0 <= this.alphaL && this.alphaL <= this.alphaU && this.alphaU <= 1.0)) {
//            System.out.printf("警告: 无效的隶属度区间: [%.4f, %.4f]%n", this.alphaL, this.alphaU);
//        }
//        if (!(0 <= this.betaL && this.betaL <= this.betaU && this.betaU <= 1.0)) {
//            System.out.printf("警告: 无效的非隶属度区间: [%.4f, %.4f]%n", this.betaL, this.betaU);
//        }
    }

    // Getters
    public double getAlphaL() { return alphaL; }
    public double getAlphaU() { return alphaU; }
    public double getBetaL() { return betaL; }
    public double getBetaU() { return betaU; }

    /**
     * 费马模糊数加法
     *
     * @param other 另一个费马模糊数
     * @return 相加后的新费马模糊数
     */
    public FermateanFuzzyNumber add(FermateanFuzzyNumber other) {
        double alphaL_new = Math.cbrt(Math.pow(this.alphaL, 3) + Math.pow(other.alphaL, 3) - (Math.pow(this.alphaL, 3) * Math.pow(other.alphaL, 3)));
        double alphaU_new = Math.cbrt(Math.pow(this.alphaU, 3) + Math.pow(other.alphaU, 3) - (Math.pow(this.alphaU, 3) * Math.pow(other.alphaU, 3)));
        double betaL_new = this.betaL * other.betaL;
        double betaU_new = this.betaU * other.betaU;
        return new FermateanFuzzyNumber(new double[]{alphaL_new, alphaU_new}, new double[]{betaL_new, betaU_new});
    }

    /**
     * 费马模糊数乘法
     *
     * @param other 另一个费马模糊数
     * @return 相乘后的新费马模糊数
     */
    public FermateanFuzzyNumber multiply(FermateanFuzzyNumber other) {
        double alphaL_new = this.alphaL * other.alphaL;
        double alphaU_new = this.alphaU * other.alphaU;
        double betaL_new = Math.cbrt(Math.pow(this.betaL, 3) + Math.pow(other.betaL, 3) - (Math.pow(this.betaL, 3) * Math.pow(other.betaL, 3)));
        double betaU_new = Math.cbrt(Math.pow(this.betaU, 3) + Math.pow(other.betaU, 3) - (Math.pow(this.betaU, 3) * Math.pow(other.betaU, 3)));
        return new FermateanFuzzyNumber(new double[]{alphaL_new, alphaU_new}, new double[]{betaL_new, betaU_new});
    }

    /**
     * 费马模糊数与标量（普通数）的乘法
     *
     * @param lambdaVal 标量值
     * @return 相乘后的新费马模糊数
     */
    public FermateanFuzzyNumber scalarMultiply(double lambdaVal) {
        if (lambdaVal < 0) {
           // System.out.printf("警告: 标量乘法对于 lambda < 0 (%f) 的定义不是非常稳健。%n", lambdaVal);
            if (lambdaVal == -1) {
                // 用于减法的特殊取反操作
                return new FermateanFuzzyNumber(new double[]{this.betaL, this.betaU}, new double[]{this.alphaL, this.alphaU});
            }
            lambdaVal = Math.abs(lambdaVal);
        }

        double alphaL_new = Math.cbrt(1.0 - Math.pow(1.0 - Math.pow(this.alphaL, 3), lambdaVal));
        double alphaU_new = Math.cbrt(1.0 - Math.pow(1.0 - Math.pow(this.alphaU, 3), lambdaVal));
        double betaL_new = Math.pow(this.betaL, lambdaVal);
        double betaU_new = Math.pow(this.betaU, lambdaVal);

        return new FermateanFuzzyNumber(new double[]{alphaL_new, alphaU_new}, new double[]{betaL_new, betaU_new});
    }

    /**
     * 费马模糊数的幂运算
     *
     * @param lambdaVal 指数
     * @return 幂运算后的新费马模糊数
     */
    public FermateanFuzzyNumber power(double lambdaVal) {
        if (lambdaVal == 0) return FFN_ONE;

        double alphaL_new, alphaU_new, betaL_new, betaU_new;

        if (lambdaVal < 0) {
            if (this.alphaL < EPSILON || this.alphaU < EPSILON) {
                System.out.println("警告: 尝试对接近零的FFN进行除法操作: " + this);
                return FFN_ONE; // 返回一个中性元素
            }
            alphaL_new = Math.pow(this.alphaL, lambdaVal);
            alphaU_new = Math.pow(this.alphaU, lambdaVal);
            betaL_new = Math.cbrt(1.0 - Math.pow(1.0 - Math.pow(this.betaL, 3), Math.abs(lambdaVal)));
            betaU_new = Math.cbrt(1.0 - Math.pow(1.0 - Math.pow(this.betaU, 3), Math.abs(lambdaVal)));
        } else {
            alphaL_new = Math.pow(this.alphaL, lambdaVal);
            alphaU_new = Math.pow(this.alphaU, lambdaVal);
            betaL_new = Math.cbrt(1.0 - Math.pow(1.0 - Math.pow(this.betaL, 3), lambdaVal));
            betaU_new = Math.cbrt(1.0 - Math.pow(1.0 - Math.pow(this.betaU, 3), lambdaVal));
        }

        return new FermateanFuzzyNumber(new double[]{alphaL_new, alphaU_new}, new double[]{betaL_new, betaU_new});
    }

    /**
     * 费马模糊数减法
     *
     * @param other 减数
     * @return 相减后的新费马模糊数
     */
    public FermateanFuzzyNumber subtract(FermateanFuzzyNumber other) {
        // a - b 定义为 a + (-1)*b
        FermateanFuzzyNumber negOther = other.scalarMultiply(-1);
        return this.add(negOther);
    }

    /**
     * 费马模糊数除法
     *
     * @param other 除数
     * @return 相除后的新费马模糊数
     */
    public FermateanFuzzyNumber divide(FermateanFuzzyNumber other) {
        if (other.alphaL < EPSILON && other.alphaU < EPSILON) {
            System.out.println("警告: 尝试除以一个接近零的FFN: " + other);
            return FFN_ONE; // 返回一个中性元素
        }
        // a / b 定义为 a * (b^-1)
        FermateanFuzzyNumber invOther = other.power(-1);
        return this.multiply(invOther);
    }

    /**
     * 计算得分函数 S
     */
    public double scoreS() {
        return (Math.pow(this.alphaL, 3) + Math.pow(this.alphaU, 3) - Math.pow(this.betaL, 3) - Math.pow(this.betaU, 3) + 2) / 4.0;
    }

    /**
     * 计算得分函数 H
     */
    public double scoreH() {
        return (Math.pow(this.alphaL, 3) + Math.pow(this.alphaU, 3) + Math.pow(this.betaL, 3) + Math.pow(this.betaU, 3)) / 2.0;
    }

    /**
     * 计算得分函数 MU
     */
    public double scoreMU() {
        return (Math.pow(this.alphaU, 3) - Math.pow(this.alphaL, 3) - Math.pow(this.betaU, 3) + Math.pow(this.betaL, 3) + 1) / 2.0;
    }

    /**
     * 计算得分函数 HU
     */
    public double scoreHU() {
        return Math.pow(this.alphaU, 3) + Math.pow(this.betaU, 3) - Math.pow(this.alphaL, 3) - Math.pow(this.betaL, 3);
    }


    /**
     * 比较两个费马模糊数的大小
     *
     * @param other 要比较的另一个对象
     * @return 1: this > other, -1: this < other, 0: this == other
     */
    @Override
    public int compareTo(FermateanFuzzyNumber other) {
        double s1 = this.scoreS();
        double s2 = other.scoreS();
        if (Math.abs(s1 - s2) > EPSILON) {
            return s1 > s2 ? 1 : -1;
        }

        double h1 = this.scoreH();
        double h2 = other.scoreH();
        if (Math.abs(h1 - h2) > EPSILON) {
            return h1 > h2 ? 1 : -1;
        }

        double mu1 = this.scoreMU();
        double mu2 = other.scoreMU();
        if (Math.abs(mu1 - mu2) > EPSILON) {
            return mu1 > mu2 ? 1 : -1;
        }

        double hu1 = this.scoreHU();
        double hu2 = other.scoreHU();
        if (Math.abs(hu1 - hu2) > EPSILON) {
            // 注意这里是 hu1 > hu2 时返回 -1
            return hu1 > hu2 ? -1 : 1;
        }

        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FermateanFuzzyNumber that = (FermateanFuzzyNumber) o;
        return this.compareTo(that) == 0;
    }

    @Override
    public int hashCode() {
        // 一个简化的hashCode实现，实际应用中可能需要更复杂的实现
        return Objects.hash(alphaL, alphaU, betaL, betaU);
    }

    @Override
    public String toString() {
        return String.format("FFN(([%.4f, %.4f], [%.4f, %.4f]))", alphaL, alphaU, betaL, betaU);
    }

    /**
     * 辅助方法，用于清理计算过程中可能出现的超出 [0,1] 范围的值
     * @return 清理后的新FermateanFuzzyNumber实例
     */
    public FermateanFuzzyNumber clamp() {
        double newAlphaL = Math.max(0.0, Math.min(this.alphaL, 1.0));
        double newAlphaU = Math.max(0.0, Math.min(this.alphaU, 1.0));
        double newBetaL = Math.max(0.0, Math.min(this.betaL, 1.0));
        double newBetaU = Math.max(0.0, Math.min(this.betaU, 1.0));

        if (newAlphaL > newAlphaU) newAlphaL = newAlphaU;
        if (newBetaL > newBetaU) newBetaL = newBetaU;

        return new FermateanFuzzyNumber(new double[]{newAlphaL, newAlphaU}, new double[]{newBetaL, newBetaU});
    }
}