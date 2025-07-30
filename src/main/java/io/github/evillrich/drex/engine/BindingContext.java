package io.github.evillrich.drex.engine;

import java.util.*;

/**
 * Manages binding context during NFA simulation, building JSON structure incrementally.
 * <p>
 * BindingContext maintains a stack of nested contexts (objects and arrays) and handles
 * property binding as regex captures are made. This class is designed to support both
 * immediate JSON building (MVP) and future position tracking capabilities.
 * <p>
 * The context tracks the current binding path and allows for nested object and array
 * creation as patterns are matched.
 *
 * @since 1.0
 */
public final class BindingContext {
    
    private final Stack<ContextFrame> contextStack;
    private final List<CaptureInfo> captures;
    private Map<String, Object> rootObject;

    /**
     * Creates a new binding context for JSON building.
     */
    public BindingContext() {
        this.contextStack = new Stack<>();
        this.captures = new ArrayList<>();
        this.rootObject = new LinkedHashMap<>();
    }

    /**
     * Pushes a new object context with the specified binding name.
     * <p>
     * This creates a new JSON object that will be bound to the given property name
     * in the current context.
     *
     * @param bindName the property name to bind this object to, must not be null
     * @throws IllegalArgumentException if bindName is null or empty
     */
    public void pushObject(String bindName) {
        Objects.requireNonNull(bindName, "bindName must not be null");
        if (bindName.trim().isEmpty()) {
            throw new IllegalArgumentException("bindName must not be empty");
        }

        Map<String, Object> newObject = new LinkedHashMap<>();
        ContextFrame frame = new ContextFrame(ContextFrame.FrameType.OBJECT, bindName.trim(), newObject, null);
        
        // Add this object to the current context
        if (contextStack.isEmpty()) {
            // This is the root object
            rootObject.put(bindName.trim(), newObject);
        } else {
            ContextFrame currentFrame = contextStack.peek();
            currentFrame.addValue(bindName.trim(), newObject);
        }
        
        contextStack.push(frame);
    }

    /**
     * Pops the current object context.
     * <p>
     * This completes the current object and returns to the parent context.
     *
     * @throws IllegalStateException if there is no object context to pop
     */
    public void popObject() {
        if (contextStack.isEmpty()) {
            throw new IllegalStateException("No object context to pop");
        }
        
        ContextFrame frame = contextStack.peek();
        if (frame.getType() != ContextFrame.FrameType.OBJECT) {
            throw new IllegalStateException("Current context is not an object, it is: " + frame.getType());
        }
        
        contextStack.pop();
    }

    /**
     * Pushes a new array context with the specified binding name.
     * <p>
     * This creates a new JSON array that will be bound to the given property name
     * in the current context.
     *
     * @param bindName the property name to bind this array to, must not be null
     * @throws IllegalArgumentException if bindName is null or empty
     */
    public void pushArray(String bindName) {
        Objects.requireNonNull(bindName, "bindName must not be null");
        if (bindName.trim().isEmpty()) {
            throw new IllegalArgumentException("bindName must not be empty");
        }

        List<Object> newArray = new ArrayList<>();
        ContextFrame frame = new ContextFrame(ContextFrame.FrameType.ARRAY, bindName.trim(), null, newArray);
        
        // Add this array to the current context
        if (contextStack.isEmpty()) {
            // This is a root-level array
            rootObject.put(bindName.trim(), newArray);
        } else {
            ContextFrame currentFrame = contextStack.peek();
            currentFrame.addValue(bindName.trim(), newArray);
        }
        
        contextStack.push(frame);
    }

    /**
     * Pops the current array context.
     * <p>
     * This completes the current array and returns to the parent context.
     *
     * @throws IllegalStateException if there is no array context to pop
     */
    public void popArray() {
        if (contextStack.isEmpty()) {
            throw new IllegalStateException("No array context to pop");
        }
        
        ContextFrame frame = contextStack.peek();
        if (frame.getType() != ContextFrame.FrameType.ARRAY) {
            throw new IllegalStateException("Current context is not an array, it is: " + frame.getType());
        }
        
        contextStack.pop();
    }

    /**
     * Adds a new object to the current array context.
     * <p>
     * This is used when repeat patterns create multiple objects within an array.
     *
     * @throws IllegalStateException if the current context is not an array
     */
    public void pushArrayItem() {
        if (contextStack.isEmpty()) {
            throw new IllegalStateException("No array context available");
        }
        
        ContextFrame arrayFrame = contextStack.peek();
        if (arrayFrame.getType() != ContextFrame.FrameType.ARRAY) {
            throw new IllegalStateException("Current context is not an array, it is: " + arrayFrame.getType());
        }
        
        // Create a new object for this array item
        Map<String, Object> newObject = new LinkedHashMap<>();
        arrayFrame.getArrayContext().add(newObject);
        
        // Push the new object as the current context
        ContextFrame itemFrame = new ContextFrame(ContextFrame.FrameType.OBJECT, null, newObject, null);
        contextStack.push(itemFrame);
    }

    /**
     * Binds a property value to the current context.
     * <p>
     * This adds a property to the current object context, applying any specified
     * formatting to the value.
     *
     * @param propertyName the property name, must not be null
     * @param value the raw captured value, may be null
     * @param formatter the formatter name to apply, may be null for no formatting
     * @throws IllegalStateException if the current context is not an object
     * @throws IllegalArgumentException if propertyName is null
     */
    public void bindProperty(String propertyName, String value, String formatter) {
        Objects.requireNonNull(propertyName, "propertyName must not be null");
        
        if (contextStack.isEmpty()) {
            throw new IllegalStateException("No context available for property binding");
        }
        
        ContextFrame currentFrame = contextStack.peek();
        if (currentFrame.getType() != ContextFrame.FrameType.OBJECT) {
            throw new IllegalStateException("Cannot bind property to non-object context: " + currentFrame.getType());
        }
        
        // Apply formatter if specified
        String formattedValue = formatValue(value, formatter);
        
        // Add to current object
        currentFrame.addValue(propertyName, formattedValue);
    }

    /**
     * Records capture information for future position tracking.
     * <p>
     * This method is designed for future extension and currently just stores
     * the capture info without position data.
     *
     * @param capture the capture information to record, must not be null
     */
    public void recordCapture(CaptureInfo capture) {
        Objects.requireNonNull(capture, "capture must not be null");
        captures.add(capture);
    }

    /**
     * Returns the current JSON path for debugging and position tracking.
     *
     * @return the current path string, never null
     */
    public String getCurrentPath() {
        if (contextStack.isEmpty()) {
            return "";
        }
        
        StringBuilder path = new StringBuilder();
        boolean first = true;
        
        for (ContextFrame frame : contextStack) {
            if (frame.getBindingName() != null) {
                if (!first) {
                    path.append(".");
                }
                path.append(frame.getBindingName());
                first = false;
                
                if (frame.getType() == ContextFrame.FrameType.ARRAY) {
                    path.append("[").append(frame.getArrayContext().size()).append("]");
                }
            }
        }
        
        return path.toString();
    }

    /**
     * Converts the current binding context to a JSON-compatible Map.
     *
     * @return the JSON structure as a Map, never null
     */
    public Map<String, Object> toJsonMap() {
        return new LinkedHashMap<>(rootObject);
    }

    /**
     * Returns all recorded captures for debugging and future position tracking.
     *
     * @return an immutable list of capture information, never null
     */
    public List<CaptureInfo> getCaptures() {
        return Collections.unmodifiableList(captures);
    }

    /**
     * Returns the current stack depth for debugging.
     *
     * @return the number of contexts on the stack
     */
    public int getStackDepth() {
        return contextStack.size();
    }

    /**
     * Applies formatting to a captured value.
     * <p>
     * This is a placeholder for future formatter implementation. Currently
     * supports basic transformations and returns the value as-is for unknown formatters.
     *
     * @param value the raw value to format, may be null
     * @param formatter the formatter name, may be null
     * @return the formatted value, may be null
     */
    public String formatValue(String value, String formatter) {
        if (value == null || formatter == null || formatter.trim().isEmpty()) {
            return value;
        }
        
        // Basic formatter implementations for MVP
        String fmt = formatter.trim().toLowerCase();
        if (fmt.equals("currency()")) {
            // Remove dollar signs, commas for currency formatting
            return value.replaceAll("[$,]", "");
        }
        
        if (fmt.startsWith("parsedate(")) {
            // Future: implement date parsing
            return value;
        }
        
        if (fmt.equals("trim()")) {
            return value.trim();
        }
        
        // Return as-is for unknown formatters
        return value;
    }

    @Override
    public String toString() {
        return "BindingContext{" +
               "stackDepth=" + contextStack.size() +
               ", captureCount=" + captures.size() +
               ", currentPath='" + getCurrentPath() + '\'' +
               '}';
    }
}