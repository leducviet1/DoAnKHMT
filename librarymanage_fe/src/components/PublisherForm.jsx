const PublisherForm = ({ onSubmit, editingPublisher, onCancel }) => {
  const handleSubmit = (e) => {
    e.preventDefault();

    const form = e.currentTarget;
    const formData = new FormData(form);
    const name = String(formData.get("publisherName") || "").trim();

    if (!name) {
      alert("Vui lòng nhập tên nhà xuất bản");
      return;
    }

    onSubmit({ publisherName: name });
    form.reset();
  };

  return (
    <form
      key={editingPublisher?.publisherId || "new-publisher"}
      onSubmit={handleSubmit}
      className="publisher-form"
    >
      <input
        type="text"
        name="publisherName"
        placeholder="Nhập tên nhà xuất bản"
        defaultValue={editingPublisher?.publisherName || ""}
      />

      <button className="btn btn-primary" type="submit">
        {editingPublisher ? "Cập nhật" : "Thêm mới"}
      </button>

      {editingPublisher && (
        <button type="button" onClick={onCancel} className="btn btn-secondary">
          Hủy
        </button>
      )}
    </form>
  );
};

export default PublisherForm;
