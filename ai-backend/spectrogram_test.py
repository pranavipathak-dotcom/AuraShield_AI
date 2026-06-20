import os
import numpy as np
import librosa
import librosa.display
import matplotlib.pyplot as plt

audio_path = r"D:\AuraShield_AI\dataset\archive\for-2sec\for-2seconds\training\fake"

first_file = None

for file in os.listdir(audio_path):
    if file.endswith(".wav"):
        first_file = os.path.join(audio_path, file)
        break

if first_file is None:
    raise FileNotFoundError("No .wav file found!")

print("Selected File:")
print(first_file)

# Load audio
audio, sr = librosa.load(
    first_file,
    sr=16000,
    mono=True
)

# Mel Spectrogram
mel = librosa.feature.melspectrogram(
    y=audio,
    sr=sr,
    n_mels=128
)

# Convert to dB
mel_db = librosa.power_to_db(
    mel,
    ref=np.max
)

# Plot
plt.figure(figsize=(8, 4))

librosa.display.specshow(
    mel_db,
    sr=sr,
    x_axis="time",
    y_axis="mel"
)

plt.colorbar(format="%+2.0f dB")
plt.title("Mel Spectrogram")
plt.tight_layout()

plt.savefig(
    "sample_spectrogram.png",
    dpi=300,
    bbox_inches="tight"
)

print("Saved: sample_spectrogram.png")

plt.show()