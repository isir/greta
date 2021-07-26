namespace animationparameters
{
    public class AnimationParameter
    {
        int val;
        bool mask;

        public AnimationParameter(bool mask, int value)
        {
            this.mask = mask;
            this.val = value;
        }

        public AnimationParameter()
        {
            mask = false;
            val = 0;
        }

        public AnimationParameter(AnimationParameter ap)
        {
            mask = ap.mask;
            val = ap.val;
        }

        public AnimationParameter(int va)
        {
            mask = true;
            val = va;
        }

        public void set(bool mas, int va)
        {
            val = va;
            mask = mas;
        }

        public void setValue(int va)
        {
            val = va;
        }

        public void applyValue(int va)
        {
            val = va;
            mask = true;
        }

        public void setMask(bool mas)
        {
            mask = mas;
        }

        public bool getMask()
        {
            return mask;
        }

        public int getValue()
        {
            return val;
        }
    }
}
