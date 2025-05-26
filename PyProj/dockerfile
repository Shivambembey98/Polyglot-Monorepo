# Use Python base image
FROM python:3.9

# Set the working directory
WORKDIR /app

# Copy the application files
COPY . .

# Set permissions (optional)
RUN chmod +x app.py

# Run the CLI app
CMD ["python3", "app.py"]