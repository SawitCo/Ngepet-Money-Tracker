extends Area3D

var triggered := false

func _ready():
	body_entered.connect(_on_body_entered)

func _on_body_entered(body):
	if triggered:
		return

	if not body.is_in_group("player"):
		return

	triggered = true
	print("LEVEL SELESAI")

	await get_tree().create_timer(0.5).timeout
	LevelManager.next_level()
