package archives.tater.phantomfall.render.state;

public class PhantomBodyRenderState {
    public boolean hasPhantom = false;
    public float pitch = 0f;
    public float yaw = 0f;
    public int size = 0;

    public interface Holder {
        PhantomBodyRenderState phantomfall$getPhantomBodyData();
    }
}
