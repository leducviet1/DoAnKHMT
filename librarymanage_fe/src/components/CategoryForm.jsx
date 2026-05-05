import { useState } from "react";

function CategoryForm({ onSubmit, selected }) {
  const [name, setName] = useState(selected?.name || "");

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit({ categoryName: name }); // 🔥 gửi đúng format BE
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="text"
        placeholder="Nhập tên category"
        value={name}
        onChange={(e) => setName(e.target.value)}
      />

      <button type="submit">
        {selected ? "Cập nhật" : "Thêm"}
      </button>
    </form>
  );
}

export default CategoryForm;