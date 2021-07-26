using UnityEngine;
using System.Collections;

/// <summary>
/// Test script to make an object constantly move for point A to point B.
/// </summary>
public class TestGazeMovingObject : MonoBehaviour
{
	public Vector3 startPosition;
	public Vector3 turnBackPosition;
	public float speed = 2;

	private bool _hasTurned;

	// Use this for initialization
	void Start ()
	{
		transform.position = startPosition;
	}
	
	// Update is called once per frame
	void Update () {
		// Move our position a step closer to the target.
		float step =  speed * Time.deltaTime; // calculate distance to move
		transform.position = Vector3.MoveTowards(transform.position,
			_hasTurned ? startPosition : turnBackPosition, step);

		// Check if the position of the cube and sphere are approximately equal.
		if (!_hasTurned && Vector3.Distance(transform.position, turnBackPosition) < 0.001f
		    || _hasTurned && Vector3.Distance(transform.position, startPosition) < 0.001f)
		{
			_hasTurned = !_hasTurned;
		}
	}
}
