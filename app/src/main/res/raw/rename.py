import os



def rename_files_in_current_directory():
    # Lấy thư mục hiện tại
    current_directory = os.path.dirname(os.path.realpath(__file__))

    # Duyệt qua tất cả các tệp trong thư mục
    for filename in os.listdir(current_directory):
        # Kiểm tra nếu tên tệp không chứa "black"
        if "rock" in filename:
            # Tạo tên mới
            new_name = filename.replace('rock', 'rook');
            # Đổi tên tệp
            os.rename(os.path.join(current_directory, filename), os.path.join(current_directory, new_name))
            print(f"Renamed '{filename}' to '{new_name}'")

# Gọi hàm
rename_files_in_current_directory()
