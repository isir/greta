/*
 * This file is part of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package greta.core.animation.common.easingcurve;

/**
 *
 * @author Jing Huang
 */
public class EquationFunctions {

    /**
     * Easing equation function for a simple linear tweening, with no easing.
     *
     * @param t	Current time (in frames or seconds).
     * @return	The correct value.
     */
    public static double easeNone(double t) {
        return t;
    }

    /**
     * Easing equation function for a quadratic (t^2) easing in: accelerating
     * from zero velocity.
     *
     * @param t	Current time (in frames or seconds).
     * @return	The correct value.
     */
    public static double easeInQuad(double t) {
        return t * t;
    }

    /**
     * Easing equation function for a quadratic (t^2) easing out: decelerating
     * to zero velocity.
     *
* @param t	Current time (in frames or seconds).
     * @return	The correct value.
     */
    public static double easeOutQuad(double t) {
        return -t * (t - 2);
    }

    /**
     * Easing equation function for a quadratic (t^2) easing in/out:
     * acceleration until halfway, then deceleration.
     *
     * @param t	Current time (in frames or seconds).
     * @return	The correct value.
     */
    public static double easeInOutQuad(double t) {
        t *= 2.0;
        if (t < 1) {
            return t * t / (2.0f);
        } else {
            --t;
            return -0.5f * (t * (t - 2.0f) - 1.0f);
        }
    }

    /**
     * Easing equation function for a quadratic (t^2) easing out/in:
     * deceleration until halfway, then acceleration.
     *
     * @param t	Current time (in frames or seconds).
     * @return	The correct value.
     */
    public static double easeOutInQuad(double t) {
        if (t < 0.5) {
            return easeOutQuad(t * 2) / 2;
        }
        return easeInQuad((2 * t) - 1) / 2 + 0.5f;
    }

    /**
     * Easing equation function for a cubic (t^3) easing in: accelerating from
     * zero velocity.
     *
     * @param t	Current time (in frames or seconds).
     * @return	The correct value.
     */
    public static double easeInCubic(double t) {
        return t * t * t;
    }

    /**
     * Easing equation function for a cubic (t^3) easing out: decelerating from
     * zero velocity.
     *
     * @param t	Current time (in frames or seconds).
     * @return	The correct value.
     */
    public static double easeOutCubic(double t) {
        t -= 1.0;
        return t * t * t + 1;
    }

    /**
     * Easing equation function for a cubic (t^3) easing in/out: acceleration
     * until halfway, then deceleration.
     *
     * @param t	Current time (in frames or seconds).
     * @return	The correct value.
     */
    public static double easeInOutCubic(double t) {
        t *= 2.0;
        if (t < 1) {
            return 0.5f * t * t * t;
        } else {
            t -= (2.0f);
            return 0.5f * (t * t * t + 2);
        }
    }

    /**
     * Easing equation function for a cubic (t^3) easing out/in: deceleration
     * until halfway, then acceleration.
     *
     * @param t	Current time (in frames or seconds).
     * @return	The correct value.
     */
    public static double easeOutInCubic(double t) {
        if (t < 0.5) {
            return easeOutCubic(2 * t) / 2;
        }
        return easeInCubic(2 * t - 1) / 2 + 0.5f;
    }

    /**
     * Easing equation function for a quartic (t^4) easing in: accelerating from
     * zero velocity.
     *
     * @param t	Current time (in frames or seconds).
     * @return	The correct value.
     */
    public static double easeInQuart(double t) {
        return t * t * t * t;
    }

    /**
     * Easing equation function for a quartic (t^4) easing out: decelerating
     * from zero velocity.
     *
     * @param t	Current time (in frames or seconds).
     * @return	The correct value.
     */
    public static double easeOutQuart(double t) {
        t -= (1.0f);
        return -(t * t * t * t - 1);
    }

    /**
     * Easing equation function for a quartic (t^4) easing in/out: acceleration
     * until halfway, then deceleration.
     *
     * @param t	Current time (in frames or seconds).
     * @return	The correct value.
     */
    public static double easeInOutQuart(double t) {
        t *= 2;
        if (t < 1) {
            return 0.5f * t * t * t * t;
        } else {
            t -= 2.0f;
            return -0.5f * (t * t * t * t - 2);
        }
    }

    /**
     * Easing equation function for a quartic (t^4) easing out/in: deceleration
     * until halfway, then acceleration.
     *
     * @param t	Current time (in frames or seconds).
     * @return	The correct value.
     */
    public static double easeOutInQuart(double t) {
        if (t < 0.5) {
            return easeOutQuart(2 * t) / 2;
        }
        return easeInQuart(2 * t - 1) / 2 + 0.5f;
    }

    /**
     * Easing equation function for a quintic (t^5) easing in: accelerating from
     * zero velocity.
     *
     * @param t	Current time (in frames or seconds).
     * @return	The correct value.
     */
    public static double easeInQuint(double t) {
        return t * t * t * t * t;
    }

    /**
     * Easing equation function for a quintic (t^5) easing out: decelerating
     * from zero velocity.
     *
     * @param t	Current time (in frames or seconds).
     * @return	The correct value.
     */
    public static double easeOutQuint(double t) {
        t -= 1.0;
        return t * t * t * t * t + 1;
    }

    /**
     * Easing equation function for a quintic (t^5) easing in/out: acceleration
     * until halfway, then deceleration.
     *
     * @param t	Current time (in frames or seconds).
     * @return	The correct value.
     */
    public static double easeInOutQuint(double t) {
        t *= 2.0;
        if (t < 1) {
            return 0.5f * t * t * t * t * t;
        } else {
            t -= 2.0;
            return 0.5f * (t * t * t * t * t + 2);
        }
    }

    /**
     * Easing equation function for a quintic (t^5) easing out/in: deceleration
     * until halfway, then acceleration.
     *
     * @param t	Current time (in frames or seconds).
     * @return	The correct value.
     */
    public static double easeOutInQuint(double t) {
        if (t < 0.5) {
            return easeOutQuint(2 * t) / 2;
        }
        return easeInQuint(2 * t - 1) / 2 + 0.5f;
    }

    /**
     * Easing equation function for a sinusoidal (sin(t)) easing in:
     * accelerating from zero velocity.
     *
     * @param t	Current time (in frames or seconds).
     * @return	The correct value.
     */
    public static double easeInSine(double t) {
        return (double) ((t == 1.0) ? 1.0 : -java.lang.Math.cos((double) (t * java.lang.Math.PI * 0.5f)) + 1.0f);
    }

    /**
     * Easing equation function for a sinusoidal (sin(t)) easing out:
     * decelerating from zero velocity.
     *
     * @param t	Current time (in frames or seconds).
     * @return	The correct value.
     */
    public static double easeOutSine(double t) {
        return (double) java.lang.Math.sin(t * java.lang.Math.PI * 0.5f);
    }

    /**
     * Easing equation function for a sinusoidal (sin(t)) easing in/out:
     * acceleration until halfway, then deceleration.
     *
     * @param t	Current time (in frames or seconds).
     * @return	The correct value.
     */
    public static double easeInOutSine(double t) {
        return (double) (-0.5 * (java.lang.Math.cos(java.lang.Math.PI * t) - 1));
    }

    /**
     * Easing equation function for a sinusoidal (sin(t)) easing out/in:
     * deceleration until halfway, then acceleration.
     *
     * @param t	Current time (in frames or seconds).
     * @return	The correct value.
     */
    public static double easeOutInSine(double t) {
        if (t < 0.5) {
            return easeOutSine(2 * t) / 2;
        }
        return easeInSine(2 * t - 1) / 2 + 0.5f;
    }

    /**
     * Easing equation function for an exponential (2^t) easing in: accelerating
     * from zero velocity.
     *
     * @param t	Current time (in frames or seconds).
     * @return	The correct value.
     */
    public static double easeInExpo(double t) {
        return (double) ((t == 0 || t == 1.0) ? t : java.lang.Math.pow(2.0, 10 * (t - 1))
                - (0.001f));
    }

    /**
     * Easing equation function for an exponential (2^t) easing out:
     * decelerating from zero velocity.
     *
     * @param t	Current time (in frames or seconds).
     * @return	The correct value.
     */
    public static double easeOutExpo(double t) {
        return (double) ((t == 1.0) ? 1.0 : 1.001 * (-java.lang.Math.pow(2.0f, -10 * t) + 1));
    }

    /**
     * Easing equation function for an exponential (2^t) easing in/out:
     * acceleration until halfway, then deceleration.
     *
     * @param t	Current time (in frames or seconds).
     * @return	The correct value.
     */
    public static double easeInOutExpo(double t)
{
    if (t==0.0) return (0.0f);
    if (t==1.0) return (1.0f);
    t*=2.0;
    if (t < 1) return (double) (0.5f * java.lang.Math.pow((2.0), 10 * (t - 1)) - 0.0005);
    return (double) (0.5f * 1.0005f * (-java.lang.Math.pow((2.0), -10 * (t - 1)) + 2));
}

/**
 * Easing equation function for an exponential (2^t) easing out/in: deceleration until halfway, then acceleration.
 *
 * @param t		Current time (in frames or seconds).
 * @return		The correct value.
 */
public static double easeOutInExpo(double t)
{
    if (t < 0.5) return easeOutExpo (2*t)/2;
    return easeInExpo(2*t - 1)/2 + 0.5f;
}

/**
 * Easing equation function for a circular (sqrt(1-t^2)) easing in: accelerating from zero velocity.
 *
 * @param t		Current time (in frames or seconds).
 * @return		The correct value.
 */
public static double easeInCirc(double t)
{
    return (double) -(java.lang.Math.sqrt(1 - t*t) - 1);
}

/**
 * Easing equation function for a circular (sqrt(1-t^2)) easing out: decelerating from zero velocity.
 *
 * @param t		Current time (in frames or seconds).
 * @return		The correct value.
 */
public static double easeOutCirc(double t)
{
    t-= (1.0);
    return (double) java.lang.Math.sqrt(1 - t* t);
}

/**
 * Easing equation function for a circular (sqrt(1-t^2)) easing in/out: acceleration until halfway, then deceleration.
 *
 * @param t		Current time (in frames or seconds).
 * @return		The correct value.
 */
public static double easeInOutCirc(double t)
{
    t*=(2.0);
    if (t < 1) {
        return (double) (-0.5 * (java.lang.Math.sqrt(1 - t*t) - 1));
    } else {
        t -= (2.0);
        return (double) (0.5 * (java.lang.Math.sqrt(1 - t*t) + 1));
    }
}

/**
 * Easing equation function for a circular (sqrt(1-t^2)) easing out/in: deceleration until halfway, then acceleration.
 *
 * @param t		Current time (in frames or seconds).
 * @return		The correct value.
 */
public static double easeOutInCirc(double t)
{
    if (t < 0.5) return easeOutCirc (2*t)/2;
    return (double) (easeInCirc(2*t - 1)/2 + 0.5);
}

public static double easeInElastic_helper(double t, double b, double c, double d, double a, double p)
{
    if (t==0) return b;
    double t_adj = (double)t / (double)d;
    if (t_adj==1) return b+c;

    double s;
    if(a < java.lang.Math.abs(c)) {
        a = c;
        s = p / 4.0f;
    } else {
        s = (double) (p / (2 * java.lang.Math.PI) * java.lang.Math.asin(c / a));
    }

    t_adj -= 1.0f;
    return (double) (-(a*java.lang.Math.pow(2.0f,10*t_adj) * java.lang.Math.sin( (t_adj*d-s)*(2*java.lang.Math.PI)/p )) + b);
}

/**
 * Easing equation function for an elastic (exponentially decaying sine wave) easing in: accelerating from zero velocity.
 *
 * @param t		Current time (in frames or seconds).
 * @param a		Amplitude.
 * @param p		Period.
 * @return		The correct value.
 */
public static double easeInElastic(double t, double a, double p)
{
    return easeInElastic_helper(t, 0, 1, 1, a, p);
}

public static double easeOutElastic_helper(double t, double c , double a, double p)
{
    if (t==0) return 0;
    if (t==1) return c;

    double s;
    if(a < c) {
        a = c;
        s = p / 4.0f;
    } else {
        s = (double) (p / (2 * java.lang.Math.PI) * java.lang.Math.asin(c / a));
    }

    return (double)(a*java.lang.Math.pow(2.0f, -10.0f*t) * java.lang.Math.sin( (t-s)*(2*java.lang.Math.PI)/p ) + c);
}

/**
 * Easing equation function for an elastic (exponentially decaying sine wave) easing out: decelerating from zero velocity.
 *
 * @param t		Current time (in frames or seconds).
 * @param a		Amplitude.
 * @param p		Period.
 * @return		The correct value.
 */
public static double easeOutElastic(double t, double a, double p)
{
    return easeOutElastic_helper(t, 1, a, p);
}

/**
 * Easing equation function for an elastic (exponentially decaying sine wave) easing in/out: acceleration until halfway, then deceleration.
 *
 * @param t		Current time (in frames or seconds).
 * @param a		Amplitude.
 * @param p		Period.
 * @return		The correct value.
 */
public static double easeInOutElastic(double t, double a, double p)
{
    if (t==0) return 0.0f;
    t*=2.0;
    if (t==2) return 1.0f;

    double s;
    if(a < 1.0) {
        a = 1.0f;
        s = p / 4.0f;
    } else {
        s = (double) (p / (2 * java.lang.Math.PI) * java.lang.Math.asin(1.0 / a));
    }

    if (t < 1) return (double) (-.5*(a*java.lang.Math.pow(2.0f,10*(t-1)) * java.lang.Math.sin( (t-1-s)*(2*java.lang.Math.PI)/p )));
    return (double) (a*java.lang.Math.pow(2.0f,-10*(t-1)) * java.lang.Math.sin( (t-1-s)*(2*java.lang.Math.PI)/p )*.5 + 1.0);
}

/**
 * Easing equation function for an elastic (exponentially decaying sine wave) easing out/in: deceleration until halfway, then acceleration.
 *
 * @param t		Current time (in frames or seconds).
 * @param a		Amplitude.
 * @param p		Period.
 * @return		The correct value.
 */
public static double easeOutInElastic(double t, double a, double p)
{
    if (t < 0.5) return easeOutElastic_helper(t*2,  0.5f, a, p);
    return easeInElastic_helper(2*t - 1.0f, 0.5f, 0.5f, 1.0f, a, p);
}

/**
 * Easing equation function for a back (overshooting cubic easing: (s+1)*t^3 - s*t^2) easing in: accelerating from zero velocity.
 *
 * @param t		Current time (in frames or seconds).
 * @param s		Overshoot ammount: higher s means greater overshoot (0 produces cubic easing with no overshoot, and the default value of 1.70158 produces an overshoot of 10 percent).
 * @return		The correct value.
 */
public static double easeInBack(double t, double s)
{
    return t*t*((s+1)*t - s);
}

/**
 * Easing equation function for a back (overshooting cubic easing: (s+1)*t^3 - s*t^2) easing out: decelerating from zero velocity.
 *
 * @param t		Current time (in frames or seconds).
 * @param s		Overshoot ammount: higher s means greater overshoot (0 produces cubic easing with no overshoot, and the default value of 1.70158 produces an overshoot of 10 percent).
 * @return		The correct value.
 */
public static double easeOutBack(double t, double s)
{
    t-= (1.0f);
    return t*t*((s+1)*t+ s) + 1;
}

/**
 * Easing equation function for a back (overshooting cubic easing: (s+1)*t^3 - s*t^2) easing in/out: acceleration until halfway, then deceleration.
 *
 * @param t		Current time (in frames or seconds).
 * @param s		Overshoot ammount: higher s means greater overshoot (0 produces cubic easing with no overshoot, and the default value of 1.70158 produces an overshoot of 10 percent).
 * @return		The correct value.
 */
public static double easeInOutBack(double t, double s)
{
    t *= 2.0;
    if (t < 1) {
        s *= 1.525f;
        return 0.5f*(t*t*((s+1)*t - s));
    } else {
        t -= 2;
        s *= 1.525f;
        return 0.5f*(t*t*((s+1)*t+ s) + 2);
    }
}

/**
 * Easing equation function for a back (overshooting cubic easing: (s+1)*t^3 - s*t^2) easing out/in: deceleration until halfway, then acceleration.
 *
 * @param t		Current time (in frames or seconds).
 * @param s		Overshoot ammount: higher s means greater overshoot (0 produces cubic easing with no overshoot, and the default value of 1.70158 produces an overshoot of 10 percent).
 * @return		The correct value.
 */
public static double easeOutInBack(double t, double s)
{
    if (t < 0.5) return easeOutBack (2*t, s)/2;
    return  (easeInBack(2*t - 1, s)/2 + 0.5f);
}

public static double easeOutBounce_helper(double t, double c, double a)
{
    if (t == 1.0) return c;
    double value = 0;
    if (t < (4/11.0)) {
        value = c*(7.5625f*t*t);
    } else if (t < (8/11.0)) {
        t -= (6/11.0);
        value =  -a * (1.f - (7.5625f*t*t + 0.75f)) + c;
    } else if (t < (10/11.0)) {
        t -= (9/11.0);
        value =  -a * (1.f - (7.5625f*t*t + .9375f)) + c;
    } else {
        t -= (21/22.0);
        value = -a * (1.f - (7.5625f*t*t + .984375f)) + c;
    }
    //System.out.println("t : " +t + " a : " +a + "value : " +value);
    return value;
}

/**
 * Easing equation function for a bounce (exponentially decaying parabolic bounce) easing out: decelerating from zero velocity.
 *
 * @param t		Current time (in frames or seconds).
 * @param a		Amplitude.
 * @return		The correct value.
 */
public static double easeOutBounce(double t, double a)
{
    return easeOutBounce_helper(t, 1, a);
}

/**
 * Easing equation function for a bounce (exponentially decaying parabolic bounce) easing in: accelerating from zero velocity.
 *
 * @param t		Current time (in frames or seconds).
 * @param a		Amplitude.
 * @return		The correct value.
 */
public static double easeInBounce(double t, double a)
{
    return 1.0f - easeOutBounce_helper(1.0f-t, 1.0f, a);
}


/**
 * Easing equation function for a bounce (exponentially decaying parabolic bounce) easing in/out: acceleration until halfway, then deceleration.
 *
 * @param t		Current time (in frames or seconds).
 * @param a		Amplitude.
 * @return		The correct value.
 */
public static double easeInOutBounce(double t, double a)
{
    if (t < 0.5) return easeInBounce (2*t, a)/2;
    else return (t == 1.0) ? 1.0f : easeOutBounce (2*t - 1.f, a)/2 + 0.5f;
}

/**
 * Easing equation function for a bounce (exponentially decaying parabolic bounce) easing out/in: deceleration until halfway, then acceleration.
 *
 * @param t		Current time (in frames or seconds).
 * @param a		Amplitude.
 * @return		The correct value.
 */
public static double easeOutInBounce(double t, double a)
{
    if (t < 0.5) return easeOutBounce_helper(t*2, 0.5f, a);
    return 1.0f - easeOutBounce_helper (2.0f-2*t, 0.5f, a);
}

public static  double sinProgress(double value)
{
    return (double) (java.lang.Math.sin((value * java.lang.Math.PI) - java.lang.Math.PI * 0.5f) / 2 + (0.5f));
}

public static  double smoothBeginEndMixFactor(double value)
{
    return java.lang.Math.min(java.lang.Math.max(1 - value * 2 + (0.3f), (0.0f)), (1.0f));
}

// SmoothBegin blends Smooth and Linear Interpolation.
// Progress 0 - 0.3      : Smooth only
// Progress 0.3 - ~ 0.5  : Mix of Smooth and Linear
// Progress ~ 0.5  - 1   : Linear only

/**
 * Easing function that starts growing slowly, then increases in speed. At the end of the curve the speed will be constant.
 */
public static double easeInCurve(double t)
{
     double sinProgress = sinProgress(t);
     double mix = smoothBeginEndMixFactor(t);
    return sinProgress * mix + t * (1 - mix);
}

/**
 * Easing function that starts growing steadily, then ends slowly. The speed will be constant at the beginning of the curve.
 */
public static double easeOutCurve(double t)
{
     double sinProgress = sinProgress(t);
     double mix = smoothBeginEndMixFactor(1 - t);
    return sinProgress * mix + t * (1 - mix);
}

/**
 * Easing function where the value grows sinusoidally. Note that the calculated  end value will be 0 rather than 1.
 */
public static double easeSineCurve(double t)
{
    return (double) ((java.lang.Math.sin(((t * java.lang.Math.PI * 2)) - java.lang.Math.PI * 0.5f) + 1) / 2);
}

/**
 * Easing function where the value grows cosinusoidally. Note that the calculated start value will be 0.5 and the end value will be 0.5
 * contrary to the usual 0 to 1 easing curve.
 */
public static double easeCosineCurve(double t)
{
    return (double) ((java.lang.Math.cos(((t * java.lang.Math.PI * 2)) - java.lang.Math.PI * 0.5f) + 1) / 2.0f);
}

}
