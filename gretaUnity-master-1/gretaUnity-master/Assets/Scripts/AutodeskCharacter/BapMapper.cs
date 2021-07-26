using System;
using UnityEngine;
using animationparameters;

namespace autodeskcharacter.bapmapper
{

	public class BapMapper {

		protected Transform bone;
		protected Quaternion preRotation;
		protected Quaternion postRotation;

		public BapMapper(Transform bone, Quaternion correction){
			this.bone = bone;
			Quaternion parentOrientation;
			Transform parent = bone.parent;
			if(parent == null){
				parentOrientation = Quaternion.identity;
			}
			else{
				parentOrientation = parent.rotation;
			}

			preRotation = parentOrientation * bone.localRotation;
			if (correction != Quaternion.identity)
			{
				preRotation = correction * preRotation;
			}
			postRotation = Quaternion.Inverse(parentOrientation);

		}

		public virtual void applyBap(AnimationParametersFrame bf){
			if(needsUpdate(bf)){
				bone.localRotation = postRotation * getRotation(bf) * preRotation;
			}
		}
		public virtual bool needsUpdate(AnimationParametersFrame bf){
			return false;
		}
		public virtual Quaternion getRotation(AnimationParametersFrame bf){
			return Quaternion.identity;
		}

		protected Quaternion newQuaternion(Vector3 axis, double angle){
			return Quaternion.AngleAxis ((float)(angle * 180 / System.Math.PI), axis);
		}
	}

	public class OneDOF : BapMapper{
		protected BAPType type1;
		protected Vector3 axis1;

		public OneDOF(Transform bone, BAPType type1, Vector3 axis1) : this(bone, Quaternion.identity, type1, axis1)
		{
		}

		public OneDOF(Transform bone, Quaternion correction, BAPType type1, Vector3 axis1) : base(bone, correction)
		{
			this.type1 = type1;
			this.axis1 = axis1;
		}

		public override bool needsUpdate(AnimationParametersFrame bf) {
			return bf.getMask(type1);
		}

		public override Quaternion getRotation(AnimationParametersFrame bf) {
			return newQuaternion(axis1, bf.getRadianValue(type1));
		}
	}

	public class TwoDOF : BapMapper{
		protected BAPType type1;
		protected Vector3 axis1;
		protected double lastVal1;
		protected BAPType type2;
		protected Vector3 axis2;
		protected double lastVal2;

		public TwoDOF(Transform bone, BAPType type1, Vector3 axis1, BAPType type2, Vector3 axis2) : this(bone, Quaternion.identity, type1, axis1, type2, axis2)
		{
		}
		public TwoDOF(Transform bone, Quaternion correction, BAPType type1, Vector3 axis1, BAPType type2, Vector3 axis2) : base(bone, correction){
			this.type1 = type1;
			this.axis1 = axis1;
			lastVal1 = 0;
			this.type2 = type2;
			this.axis2 = axis2;
			lastVal2 = 0;
		}

		public override bool needsUpdate(AnimationParametersFrame bf) {
			bool toReturn = false;
			if(bf.getMask(type1)){
				lastVal1 = bf.getRadianValue(type1);
				toReturn = true;
			}
			if(bf.getMask(type2)){
				lastVal2 = bf.getRadianValue(type2);
				toReturn = true;
			}
			return toReturn;
		}

		public override Quaternion getRotation(AnimationParametersFrame bf) {
			Quaternion q1 = newQuaternion(axis1, lastVal1);
			Quaternion q2 = newQuaternion(axis2, lastVal2);
			return q2 * q1;
		}
	}

	public class ThreeDOF : TwoDOF{
		protected BAPType type3;
		protected Vector3 axis3;
		protected double lastVal3;

		public ThreeDOF(Transform bone, BAPType type1, Vector3 axis1, BAPType type2, Vector3 axis2, BAPType type3, Vector3 axis3) :
		this(bone, Quaternion.identity, type1, axis1, type2, axis2, type3, axis3){
		}

		public ThreeDOF(Transform bone, Quaternion correction, BAPType type1, Vector3 axis1, BAPType type2, Vector3 axis2, BAPType type3, Vector3 axis3) :
		base(bone, correction, type1, axis1, type2, axis2){
			this.type3 = type3;
			this.axis3 = axis3;
			lastVal3 = 0;
		}

		public override bool needsUpdate(AnimationParametersFrame bf) {
			bool toReturn = base.needsUpdate(bf);
			if(bf.getMask(type3)){
				lastVal3 = bf.getRadianValue(type3);
				toReturn = true;
			}
			return toReturn;
		}

		public override Quaternion getRotation(AnimationParametersFrame bf) {
			Quaternion q1 = newQuaternion(axis1, lastVal1);
			Quaternion q2 = newQuaternion(axis2, lastVal2);
			Quaternion q3 = newQuaternion(axis3, lastVal3);

			return q3 * q2 * q1;
		}
	}

	public class Twist : BapMapper{
		public Quaternion twistRotation;
		public bool twistUpdate = false;
		public Twist(Transform bone, Quaternion correction) : base(bone, correction){
		}
		public Twist(Transform bone) : this(bone, Quaternion.identity){
		}

		public override
		bool needsUpdate(AnimationParametersFrame bf) {
			return twistUpdate;
		}

		public override
		Quaternion getRotation(AnimationParametersFrame bf) {
			return twistRotation;
		}

	}
	public class TwistMapper : ThreeDOF{

		protected Twist twist;
		protected double twistFactor = 0;

		public TwistMapper(Transform bone, Transform twistBone, BAPType type1, Vector3 axis1, BAPType type2, Vector3 axis2, BAPType type3, Vector3 axis3, double twistFactor) :
		base(bone, type1, axis1, type2, axis2, type3, axis3){
			this.twistFactor = twistFactor;
			twist = new Twist(twistBone);
		}

		public override bool needsUpdate(AnimationParametersFrame bf) {
			bool update = base.needsUpdate(bf);
			twist.twistUpdate = update;
			return update;
		}

		public override Quaternion getRotation(AnimationParametersFrame bf) {
			Quaternion original = base.getRotation(bf);
			twist.twistRotation = getTwistRotation(original);
			return propagateTwist(twist.twistRotation, original);
		}

		public override void applyBap(AnimationParametersFrame bf) {
			base.applyBap(bf);
			twist.applyBap(bf);
		}

		public virtual Quaternion propagateTwist(Quaternion twist, Quaternion original){
			return original;
		}

		public virtual Quaternion getTwistRotation(Quaternion originalRotation) {
			return Quaternion.identity;
		}

	}

	public class YawTwistMapper : TwistMapper{
		public Vector3 yawAxis = new Vector3(0, 1, 0);
		public YawTwistMapper(Transform bone, Transform twistBone, BAPType type1, Vector3 axis1, BAPType type2, Vector3 axis2, BAPType type3, Vector3 axis3, double twistFactor) :
		base(bone, twistBone, type1, axis1, type2, axis2, type3, axis3, twistFactor){
		}

		public override Quaternion getTwistRotation(Quaternion originalRotation) {
			double fTx  = 2.0*originalRotation.x;
			double fTy  = 2.0*originalRotation.y;
			double fTz  = 2.0*originalRotation.z;
			double fTwy = fTy*originalRotation.w;
			double fTxx = fTx*originalRotation.x;
			double fTxz = fTz*originalRotation.x;
			double fTyy = fTy*originalRotation.y;
			double yaw = Math.Atan2(fTxz+fTwy, 1.0-(fTxx+fTyy)) * twistFactor;

			return newQuaternion(yawAxis, yaw);
		}
	}

	public class YawTwistBeforeMapper : YawTwistMapper{

		public YawTwistBeforeMapper(Transform bone, Transform twistBone, BAPType type1, Vector3 axis1, BAPType type2, Vector3 axis2, BAPType type3, Vector3 axis3, double twistFactor) :
		base(bone, twistBone, type1, axis1, type2, axis2, type3, axis3, twistFactor){
		}

		public override Quaternion propagateTwist(Quaternion twist, Quaternion original) {
			return Quaternion.Inverse(twist) * original;
		}
	}

	public class YawTwistAfterMapper : YawTwistMapper{

		public YawTwistAfterMapper(Transform bone, Transform twistBone, BAPType type1, Vector3 axis1, BAPType type2, Vector3 axis2, BAPType type3, Vector3 axis3, double twistFactor) :
		base(bone, twistBone, type1, axis1, type2, axis2, type3, axis3, twistFactor){
		}

		public override Quaternion propagateTwist(Quaternion twist, Quaternion original) {
			return original * Quaternion.Inverse(twist);
		}
	}

	public class ThreeDOFScaled : ThreeDOF{
		private double scale = 1;
		public void setScale(double scale){
			this.scale = scale;
		}

		public ThreeDOFScaled(Transform bone, BAPType type1, Vector3 axis1, BAPType type2, Vector3 axis2, BAPType type3, Vector3 axis3) :
		this(bone, Quaternion.identity, type1, axis1, type2, axis2, type3, axis3){
		}

		public ThreeDOFScaled(Transform bone, Quaternion correction, BAPType type1, Vector3 axis1, BAPType type2, Vector3 axis2, BAPType type3, Vector3 axis3) :
		base(bone, correction, type1, axis1, type2, axis2, type3, axis3){
		}

		public override Quaternion getRotation(AnimationParametersFrame bf) {
			Quaternion q1 = newQuaternion(axis1, (lastVal1*scale));
			Quaternion q2 = newQuaternion(axis2, (lastVal2*scale));
			Quaternion q3 = newQuaternion(axis3, (lastVal3*scale));

			return q3 * q2 * q1;
		}
	}

	/*

		static class SortedTwistMapper : TwistMapper{
			public SortedTwistMapper(Transform bone, Transform twistBone, BAPType type1, Vector3 axis1, BAPType type2, Vector3 axis2, BAPType type3, Vector3 axis3, double twistFactor) {
				super(bone, twistBone, type1, axis1, type2, axis2, type3, axis3, twistFactor);
			}

			public override
			Quaternion propagateTwist(Quaternion twist, Quaternion original) {
				return Quaternion.identity;
			}

			public override
			Quaternion getTwistRotation(Quaternion originalRotation) {
				return Quaternion.identity;
			}

			public override
			Quaternion getRotation(AnimationParametersFrame bf) {
				twist.twistRotation = new Quaternion(axis1, lastVal1*twistFactor);
				Quaternion q1 = new Quaternion(axis1, lastVal1*(1-twistFactor));
				Quaternion q2 = new Quaternion(axis2, lastVal2);
				Quaternion q3 = new Quaternion(axis3, lastVal3);
				Quaternion res = Quaternion.multiplication(Quaternion.multiplication(q3,q2),q1);
				res.normalize();
				return res;
			}
		}
		static class SortedTwistBeforeMapper : TwistMapper{
			public SortedTwistBeforeMapper(Transform bone, Transform twistBone, BAPType type1, Vector3 axis1, BAPType type2, Vector3 axis2, BAPType type3, Vector3 axis3, double twistFactor) {
				super(bone, twistBone, type1, axis1, type2, axis2, type3, axis3, twistFactor);
			}

			public override
			Quaternion propagateTwist(Quaternion twist, Quaternion original) {
				return Quaternion.identity;
			}

			public override
			Quaternion getTwistRotation(Quaternion originalRotation) {
				return Quaternion.identity;
			}

			public override
			Quaternion getRotation(AnimationParametersFrame bf) {
				twist.twistRotation = new Quaternion(axis1, lastVal1*twistFactor);
				Quaternion q1 = new Quaternion(axis1, lastVal1*(1-twistFactor));
				Quaternion q2 = new Quaternion(twist.twistRotation.inverseRotate(axis2), lastVal2);
				Quaternion q3 = new Quaternion(twist.twistRotation.inverseRotate(axis3), lastVal3);
				Quaternion res = Quaternion.multiplication(Quaternion.multiplication(q3,q2),q1);
				res.normalize();
				return res;
			}
		}

		static class ShoulderSortedTwistMapper : SortedTwistMapper{

			BapMapper acromium;

			public ShoulderSortedTwistMapper(Transform bone, Transform twistBone, BAPType type1, Vector3 axis1, BAPType type2, Vector3 axis2, BAPType type3, Vector3 axis3, double twistFactor, BapMapper acromium) {
				super(bone, twistBone, type1, axis1, type2, axis2, type3, axis3, twistFactor);
				this.acromium = acromium;
			}

			public override
			bool needsUpdate(AnimationParametersFrame bf) {
				bool superNeedsUpdate = super.needsUpdate(bf);
				bool acromiumNeedsUpdate = acromium.needsUpdate(bf);
				return superNeedsUpdate || acromiumNeedsUpdate;
			}

			public override
			Quaternion getRotation(AnimationParametersFrame bf) {
				Quaternion res = Quaternion.multiplication(acromium.getRotation(bf),super.getRotation(bf));
				res.normalize();
				return res;
			}

		}
		*/
	}
