using UnityEngine;

public class CameraController : MonoBehaviour
{
    public static Vector3 target;

    public float xSpeed = 250.0f;
    public float ySpeed = 120.0f;
    public float zSpeed = 0.05f;
    public float yMinLimit = -20f;
    public float yMaxLimit = 80f;

    public int sensitivityX = 4;
    public int sensitivityY = 4;

    public float minimumX = -360F;
    public float maximumX = 360F;

    public float minimumY = -360F;
    public float maximumY = 360F;

    public float x = SceneManager.xCamera;
    public float y = SceneManager.yCamera;
    public static float z = SceneManager.zCamera;

    public Quaternion originalRotation;

    public void Update()
    {
        // rotation
        if (Input.GetMouseButton(0))
        {
            float xx = Input.GetAxis("Mouse X") * xSpeed * 0.02f;
            float yy = Input.GetAxis("Mouse Y") * ySpeed * 0.02f;
            if ((xx != 0) || (yy != 0))
            {
                x = Camera.main.transform.eulerAngles.y + xx;
                y = Camera.main.transform.eulerAngles.x - yy;

                Quaternion rotation = Quaternion.Euler(y, x, 0);
                Vector3 position = rotation * new Vector3(0.0f, 0.0f, z) + target;
                Camera.main.transform.rotation = rotation;
                Camera.main.transform.position = position;
            }
        }

        if (Input.GetMouseButton(1))
        {
            Vector3 posi = Camera.main.transform.position;
            Quaternion tmp = Quaternion.Inverse(Camera.main.transform.rotation) * originalRotation;
            posi = tmp * posi;
            target = tmp * target;

            posi.x += Input.GetAxis("Mouse X") / sensitivityX;
            posi.y -= Input.GetAxis("Mouse Y") / sensitivityX;

            target.x += Input.GetAxis("Mouse X") / sensitivityX;
            target.y -= Input.GetAxis("Mouse Y") / sensitivityY;

            posi = Quaternion.Inverse(tmp) * posi;
            target = Quaternion.Inverse(tmp) * target;

            Camera.main.transform.position = posi;
        }

        // zoom

        float zMouse = Input.GetAxis("Mouse ScrollWheel") * 1.0F;
        if (zMouse != 0)
        {
            z = z + zMouse;
            Quaternion rot = Quaternion.Euler(transform.eulerAngles.x, transform.eulerAngles.y, 0);
            Vector3 pos = rot * new Vector3(0.0f, 0.0f, z) + target;
            transform.position = pos;
        }
    }

    public void Awake()
    {
        // Make the rigid body not change rotation
        if (GetComponent<Rigidbody>())
            GetComponent<Rigidbody>().freezeRotation = true;

        // target for rotation
        target = SceneManager.iTarget;

        Vector3 angles = Camera.main.transform.eulerAngles;
        x = angles.y;
        y = angles.x;
        y = ClampAngle(y, yMinLimit, yMaxLimit);

        // init camera
        Quaternion rotation = Quaternion.Euler(y, x, 0);
        Vector3 position = rotation * new Vector3(0.0f, 0.0f, z) + target;

        Camera.main.transform.rotation = rotation;
        Camera.main.transform.position = position;

        originalRotation = Camera.main.transform.rotation = rotation;
    }

    public float ClampAngle(float angle, float min, float max)
    {
        if (angle < -360)
            angle += 360;
        if (angle > 360)
            angle -= 360;
        return Mathf.Clamp(angle, min, max);
    }
}
