package io.github.evillrich.drex.engine;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a single frame in the binding context stack.
 * <p>
 * Each ContextFrame corresponds to a level of nesting in the JSON structure being built,
 * whether it's an object context (for groups) or an array context (for repeats).
 * Frames maintain references to their underlying data structures and track their binding names.
 * <p>
 * This class is designed to support both immediate JSON building and future position tracking.
 *
 * @since 1.0
 */
final class ContextFrame {

    /**
     * The type of context frame representing different JSON structures.
     */
    enum FrameType {
        /**
         * Root context frame (top-level).
         */
        ROOT,
        
        /**
         * Object context frame (JSON object/map).
         */
        OBJECT,
        
        /**
         * Array context frame (JSON array/list).
         */
        ARRAY
    }

    private final FrameType type;
    private final String bindingName;
    private final Map<String, Object> objectContext;
    private final List<Object> arrayContext;

    /**
     * Creates a new context frame.
     *
     * @param type the frame type, must not be null
     * @param bindingName the binding name for this context, may be null for root or array items
     * @param objectContext the object context if this is an OBJECT frame, may be null
     * @param arrayContext the array context if this is an ARRAY frame, may be null
     * @throws IllegalArgumentException if type is null or if contexts don't match the frame type
     */
    ContextFrame(FrameType type, String bindingName, 
                Map<String, Object> objectContext, List<Object> arrayContext) {
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.bindingName = bindingName;
        this.objectContext = objectContext;
        this.arrayContext = arrayContext;
        
        // Validate that the context matches the frame type
        validateContexts();
    }

    /**
     * Validates that the provided contexts match the frame type.
     */
    private void validateContexts() {
        switch (type) {
            case ROOT:
                // Root can have either context type or neither
                break;
            case OBJECT:
                if (objectContext == null) {
                    throw new IllegalArgumentException("OBJECT frame must have objectContext");
                }
                if (arrayContext != null) {
                    throw new IllegalArgumentException("OBJECT frame cannot have arrayContext");
                }
                break;
            case ARRAY:
                if (arrayContext == null) {
                    throw new IllegalArgumentException("ARRAY frame must have arrayContext");
                }
                if (objectContext != null) {
                    throw new IllegalArgumentException("ARRAY frame cannot have objectContext");
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown frame type: " + type);
        }
    }

    /**
     * Returns the frame type.
     *
     * @return the frame type, never null
     */
    FrameType getType() {
        return type;
    }

    /**
     * Returns the binding name for this frame.
     *
     * @return the binding name, may be null
     */
    String getBindingName() {
        return bindingName;
    }

    /**
     * Returns the object context for this frame.
     * <p>
     * This is only valid for OBJECT frame types.
     *
     * @return the object context, may be null if this is not an OBJECT frame
     */
    Map<String, Object> getObjectContext() {
        return objectContext;
    }

    /**
     * Returns the array context for this frame.
     * <p>
     * This is only valid for ARRAY frame types.
     *
     * @return the array context, may be null if this is not an ARRAY frame
     */
    List<Object> getArrayContext() {
        return arrayContext;
    }

    /**
     * Adds a value to this frame's context.
     * <p>
     * For OBJECT frames, this adds a property to the object.
     * For ARRAY frames, this adds an item to the array.
     *
     * @param key the property key (for OBJECT frames) or null (for ARRAY frames)
     * @param value the value to add, may be null
     * @throws IllegalStateException if the frame type doesn't support adding values
     * @throws IllegalArgumentException if key is required but null, or if key is provided for array frames
     */
    void addValue(String key, Object value) {
        switch (type) {
            case OBJECT:
                if (key == null) {
                    throw new IllegalArgumentException("Key cannot be null for OBJECT frame");
                }
                objectContext.put(key, value);
                break;
                
            case ARRAY:
                if (key != null) {
                    throw new IllegalArgumentException("Key must be null for ARRAY frame");
                }
                arrayContext.add(value);
                break;
                
            case ROOT:
                throw new IllegalStateException("Cannot add values directly to ROOT frame");
                
            default:
                throw new IllegalStateException("Unknown frame type: " + type);
        }
    }

    /**
     * Returns the current size of this frame's context.
     * <p>
     * For OBJECT frames, returns the number of properties.
     * For ARRAY frames, returns the number of items.
     *
     * @return the size of the current context, or 0 for ROOT frames
     */
    int size() {
        switch (type) {
            case OBJECT:
                return objectContext != null ? objectContext.size() : 0;
            case ARRAY:
                return arrayContext != null ? arrayContext.size() : 0;
            case ROOT:
                return 0;
            default:
                return 0;
        }
    }

    /**
     * Returns whether this frame's context is empty.
     *
     * @return true if the context has no items, false otherwise
     */
    boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ContextFrame{");
        sb.append("type=").append(type);
        
        if (bindingName != null) {
            sb.append(", bindingName='").append(bindingName).append('\'');
        }
        
        sb.append(", size=").append(size());
        sb.append('}');
        
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ContextFrame that = (ContextFrame) obj;
        return type == that.type &&
               Objects.equals(bindingName, that.bindingName) &&
               Objects.equals(objectContext, that.objectContext) &&
               Objects.equals(arrayContext, that.arrayContext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, bindingName, objectContext, arrayContext);
    }
}