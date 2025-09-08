import React, { useRef, useState } from "react";
import { fabric } from "fabric";

function PhotoEditorApp() {
  const canvasRef = useRef();
  const [canvas, setCanvas] = useState(null);

  // Initialize Fabric.js Canvas
  React.useEffect(() => {
    const fabricCanvas = new fabric.Canvas(canvasRef.current, {
      width: 600,
      height: 400,
      backgroundColor: "#ddd"
    });
    setCanvas(fabricCanvas);
    return () => fabricCanvas.dispose();
  }, []);

  // Load image
  function handleImageUpload(e) {
    const file = e.target.files[0];
    if (!file || !canvas) return;
    const reader = new FileReader();
    reader.onload = function (f) {
      fabric.Image.fromURL(f.target.result, img => {
        img.set({ left: 100, top: 50, scaleX: 0.5, scaleY: 0.5 });
        canvas.clear();
        canvas.add(img);
        canvas.setActiveObject(img);
        canvas.renderAll();
      });
    };
    reader.readAsDataURL(file);
  }

  // Apply filter
  function applyFilter(type) {
    const obj = canvas.getActiveObject();
    if (obj && obj.type === "image") {
      switch (type) {
        case "grayscale":
          obj.filters = [new fabric.Image.filters.Grayscale()];
          break;
        case "invert":
          obj.filters = [new fabric.Image.filters.Invert()];
          break;
        case "brightness":
          obj.filters = [new fabric.Image.filters.Brightness({ brightness: 0.15 })];
          break;
        default:
          obj.filters = [];
      }
      obj.applyFilters();
      canvas.renderAll();
    }
  }

  // Crop (simple: just cut out selection)
  function cropImage() {
    const obj = canvas.getActiveObject();
    if (obj && obj.type === "image") {
      const cropped = new fabric.Image(obj.getElement(), {
        left: obj.left,
        top: obj.top,
        width: obj.width * obj.scaleX / 2,
        height: obj.height * obj.scaleY / 2,
        scaleX: obj.scaleX,
        scaleY: obj.scaleY,
        cropX: obj.width / 4,
        cropY: obj.height / 4,
      });
      canvas.clear();
      canvas.add(cropped);
      canvas.setActiveObject(cropped);
      canvas.renderAll();
    }
  }

  // Rotate
  function rotateImage() {
    const obj = canvas.getActiveObject();
    if (obj) {
      obj.rotate((obj.angle || 0) + 90);
      canvas.renderAll();
    }
  }

  // Add text
  function addText() {
    const text = new fabric.Textbox("Sample Text", {
      left: 200,
      top: 300,
      fontSize: 32,
      fill: "#222"
    });
    canvas.add(text);
    canvas.setActiveObject(text);
    canvas.renderAll();
  }

  return (
    <div>
      <h1>Photo Editor App</h1>
      <input type="file" accept="image/*" onChange={handleImageUpload} />
      <div>
        <button onClick={() => applyFilter("grayscale")}>Grayscale</button>
        <button onClick={() => applyFilter("invert")}>Invert</button>
        <button onClick={() => applyFilter("brightness")}>Brightness</button>
        <button onClick={() => applyFilter("")}>Remove Filters</button>
        <button onClick={cropImage}>Crop</button>
        <button onClick={rotateImage}>Rotate</button>
        <button onClick={addText}>Add Text</button>
      </div>
      <canvas ref={canvasRef} id="canvas" />
    </div>
  );
}

export default PhotoEditorApp;
