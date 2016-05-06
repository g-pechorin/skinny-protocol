
protocol UpDoot {
  def spawn(id: int32, parent: int32)
  def move(id: int32, x: single, y: single, z: single)
  def close(id: int32)
}
