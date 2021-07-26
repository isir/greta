using UnityEngine;
using animationparameters;

namespace autodeskcharacter.fapmapper
{
	public class FapMapper {

		protected Transform faceBone;

		public FapMapper(Transform faceBone){
			this.faceBone = faceBone;
		}
		public virtual void applyFap(AnimationParametersFrame ff){
		}

	}

	public class Lip : FapMapper {

		protected FAPType axis1Type;
		protected Vector3 direction1;
		protected FAPType axis2Type;
		protected Vector3 direction2;
		protected FAPType innerType;
		protected Vector3 rotationAxis;

		protected Vector3 position;
		protected Quaternion orientinitial;
		protected float magicNumber = 0.0008f*Mathf.Rad2Deg;

		public Lip(Transform faceBone, FAPType outerType, Vector3 directionAxis, FAPType outerType2, Vector3 directionAxis2, FAPType innerType, Vector3 rotationAxis) : base (faceBone){
			this.axis1Type = outerType;
			this.direction1 = directionAxis;
			this.axis2Type = outerType2;
			this.direction2 = directionAxis2;
			this.innerType = innerType;
			this.rotationAxis = rotationAxis;

			position = faceBone.localPosition;
			this.orientinitial = faceBone.localRotation;

		}

		public override void applyFap(AnimationParametersFrame ff) {
			if(ff.getMask(axis1Type) ||ff.getMask(axis2Type)){
				faceBone.localPosition =
					new Vector3(
						position.x + direction1.x * ff.getValue(axis1Type) + direction2.x * ff.getValue(axis2Type),
						position.y + direction1.y * ff.getValue(axis1Type) + direction2.y * ff.getValue(axis2Type),
						position.z + direction1.z * ff.getValue(axis1Type) + direction2.z * ff.getValue(axis2Type));
			}
			if(ff.getMask(innerType)){
				float dist = ff.getValue(innerType);
				Quaternion q = Quaternion.AngleAxis (dist * magicNumber, rotationAxis);
				faceBone.localRotation = q * orientinitial;
			}
		}

	}

	public class MidLip : Lip {

		FAPType stretchType;
		Vector3 stretchDirection;
		public MidLip(Transform faceBone, FAPType outerType, Vector3 directionAxis, FAPType outerType2, Vector3 directionAxis2, FAPType innerType, Vector3 rotationAxis, FAPType stretchType, Vector3 stretchDirection) :
			base(faceBone, outerType, directionAxis, outerType2, directionAxis2, innerType, rotationAxis){
			this.stretchType = stretchType;
			this.stretchDirection = stretchDirection;
		}

		public override void applyFap(AnimationParametersFrame ff) {

			if(ff.getMask(axis1Type) || ff.getMask(axis2Type) || ff.getMask(stretchType)){
				faceBone.localPosition =
					new Vector3(
						position.x+
						direction1.x*ff.getValue(axis1Type)+
						direction2.x*ff.getValue(axis2Type)+
						stretchDirection.x*ff.getValue(stretchType),
						position.y+
						direction1.y*ff.getValue(axis1Type)+
						direction2.y*ff.getValue(axis2Type)+
						stretchDirection.y*ff.getValue(stretchType),
						position.z+
						direction1.z*ff.getValue(axis1Type)+
						direction2.z*ff.getValue(axis2Type)+
						stretchDirection.z*ff.getValue(stretchType));
			}
			if(ff.getMask(innerType)){
				float dist = ff.getValue(innerType);
				Quaternion q = Quaternion.AngleAxis (dist * magicNumber, rotationAxis);
				faceBone.localRotation = q * orientinitial;
			}
		}
	}

	public class Rotation : FapMapper{

		protected FAPType pitch;
		protected Vector3 pitchAxis;

		protected FAPType yaw;
		protected Vector3 yawAxis;

		protected Quaternion orientInitial;

		protected float amplitude1;
		protected float amplitude2;

		public Rotation(Transform faceBone, FAPType pitch, Vector3 pitchAxis, float amplitude1, FAPType yaw, Vector3 yawAxis, float amplitude2) : base(faceBone){
			this.pitch = pitch;
			this.pitchAxis = pitchAxis;
			this.yaw = yaw;
			this.yawAxis = yawAxis;
			this.orientInitial = faceBone.localRotation;
			this.amplitude1 = amplitude1*Mathf.Rad2Deg;
			this.amplitude2 = amplitude2*Mathf.Rad2Deg;
		}

		public override void applyFap(AnimationParametersFrame ff) {
			if(ff.getMask(pitch) || ff.getMask(yaw)){
				Quaternion qx = Quaternion.AngleAxis (
					ff.getValue(pitch)*amplitude1,
					pitchAxis);

				Quaternion qy =Quaternion.AngleAxis (
					ff.getValue(yaw)*amplitude2,
					yawAxis);
				faceBone.localRotation = qy * qx * orientInitial;
			}
		}
	}

	public class Eye : Rotation{
		public Eye(Transform faceBone, FAPType pitch, Vector3 pitchAxis, FAPType yaw, Vector3 yawAxis) : base(faceBone, pitch, pitchAxis, (float)(1.0/100000.0), yaw, yawAxis, (float)(1.0/100000.0)){
		}
	}

	public class OneDOF : FapMapper{

		protected FAPType type1;
		protected Vector3 dir;
		protected Vector3 position;
		public OneDOF(Transform faceBone, FAPType type1, Vector3 dir) : base (faceBone){
			this.dir = dir;
			this.type1 = type1;
			position = faceBone.localPosition;
		}

		public override void applyFap(AnimationParametersFrame ff){
			if(ff.getMask(type1)){
				faceBone.localPosition = new Vector3(
					position.x+dir.x*ff.getValue(type1),
					position.y+dir.y*ff.getValue(type1),
					position.z+dir.z*ff.getValue(type1));
			}
		}

	}

	public class TwoDOF : OneDOF{

		protected FAPType type2;
		protected Vector3 dir2;

		public TwoDOF(Transform faceBone, FAPType type1, Vector3 dir, FAPType type2, Vector3 dir2) : base(faceBone, type1, dir){
			this.faceBone = faceBone;
			this.dir2 = dir2;
			this.type2 = type2;
		}

		public override void applyFap(AnimationParametersFrame ff){
			faceBone.localPosition = new Vector3(
				position.x+dir.x*ff.getValue(type1) + dir2.x*ff.getValue(type2),
				position.y+dir.y*ff.getValue(type1) + dir2.y*ff.getValue(type2),
				position.z+dir.z*ff.getValue(type1) + dir2.z*ff.getValue(type2));
		}

	}

	public class Nostril : FapMapper {

		protected FAPType type;
		protected FAPType type2;
		protected Vector3 dir;

		public Nostril(Transform faceBone, FAPType fapType, FAPType fapType2, Vector3 scaleDirection) : base(faceBone){
			type = fapType;
			type2 = fapType2;
			dir = scaleDirection;
		}

		public override void applyFap(AnimationParametersFrame ff) {
			if(ff.getMask(type) || ff.getMask(type2)){
				float fapvaltouse = 0;
				if(ff.getMask(type)){
					if(ff.getMask(type2)){
						fapvaltouse = Mathf.Max(ff.getValue(type),ff.getValue(type2));
					}
					else{
						fapvaltouse = ff.getValue(type);
					}
				}
				else{
					ff.getValue(type2);
				}
				faceBone.localScale = new Vector3(
					1+dir.x*fapvaltouse,
					1+dir.y*fapvaltouse,
					1+dir.z*fapvaltouse);
			}
		}

	}

	public class Jaw : Rotation {
		protected OneDOF translation;

		public Jaw(Transform faceBone, FAPType pitch, Vector3 pitchAxis, float amplitude1, FAPType yaw, Vector3 yawAxis, float amplitude2, FAPType thrustType, Vector3 thrustDirection) : base(faceBone, pitch, pitchAxis, amplitude1, yaw, yawAxis, amplitude2){
			translation = new OneDOF(faceBone, thrustType, thrustDirection);
		}

		public override void applyFap(AnimationParametersFrame ff) {
			translation.applyFap(ff);
			base.applyFap(ff);
		}

	}

}

